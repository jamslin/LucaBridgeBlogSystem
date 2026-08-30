package com.lucabridge.core.media;

import com.lucabridge.core.error.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * No live DB or S3: MediaRepository and MediaStorage are mocks, and the transaction manager is
 * a trivial fake that just runs the callback inline — real transaction demarcation isn't the
 * point here, call ordering is. That is enough to pin the bug this class exists to prevent: JPA
 * defers {@code repository.delete()} to commit/flush time, so a naive "delete row, then delete
 * object" method body actually runs the S3 delete first at runtime.
 */
class MediaServiceTest {

    private MediaRepository mediaRepository;
    private MediaStorage mediaStorage;
    private MediaService service;
    private List<String> callOrder;

    @BeforeEach
    void setUp() {
        mediaRepository = mock(MediaRepository.class);
        mediaStorage = mock(MediaStorage.class);
        callOrder = new ArrayList<>();

        Media media = Media.builder().id(1L).s3Key("abc-photo.jpg").build();
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));
        when(mediaRepository.countUsage(1L)).thenReturn(0L);
        // Real Hibernate defers repository.delete() to flush/commit time, so the marker for
        // "the row is actually gone" belongs on flush() — recording it on delete() instead
        // would pass even against the original bug, since that call was never the problem.
        doAnswer(inv -> {
            callOrder.add("db-delete");
            return null;
        }).when(mediaRepository).flush();
        doAnswer(inv -> {
            callOrder.add("s3-delete");
            return null;
        }).when(mediaStorage).delete("abc-photo.jpg");

        service = new MediaService(mediaRepository, mediaStorage, immediateTransactionManager());
    }

    @Test
    @DisplayName("the DB row is gone before the storage delete fires, never the reverse")
    void deletesTheDbRowBeforeTheStorageObject() {
        service.delete(1L);

        assertEquals(List.of("db-delete", "s3-delete"), callOrder);
        verify(mediaStorage).delete("abc-photo.jpg");
    }

    @Test
    @DisplayName("a still-referenced image is refused before either delete runs")
    void refusesWhenStillReferenced() {
        when(mediaRepository.countUsage(1L)).thenReturn(2L);

        assertThrows(ConflictException.class, () -> service.delete(1L));

        verify(mediaRepository, never()).delete(any(Media.class));
        verify(mediaStorage, never()).delete(any());
    }

    /**
     * Runs the {@link org.springframework.transaction.support.TransactionCallback} inline with
     * no real resource — sufficient for asserting call order against mocks, which is all this
     * test needs from the transaction manager.
     */
    private static PlatformTransactionManager immediateTransactionManager() {
        PlatformTransactionManager tm = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(tm.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        return tm;
    }
}
