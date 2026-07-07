// LucaBridge CI/CD — Jenkins owns the full pipeline including deploy.
// GitHub Actions (.github/workflows/ci.yml) is only a lightweight PR test gate.
//
// Jenkins is never in the serving or publishing path: the site reads live from
// the DB via SSR. This pipeline runs on CODE changes only.
//
// Agent requirements (Global Tool Configuration): JDK 'jdk21', NodeJS 'node20',
// plus Docker available on the agent (for Testcontainers and image builds).
// For the single-box setup, run the Jenkins agent on the same host that serves
// the site — 'Deploy' then just recreates the compose stack locally.

pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    tools {
        jdk 'jdk21'
        nodejs 'node20'
    }

    parameters {
        booleanParam(name: 'DEPLOY', defaultValue: true,
                     description: 'Deploy the compose stack after a green build (main only)')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend: test') {
            steps {
                dir('backend') {
                    sh 'chmod +x mvnw'
                    sh './mvnw -B verify'   // includes Testcontainers integration tests
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Frontend: build') {
            steps {
                dir('frontend') {
                    sh 'npm ci || npm install'
                    sh 'npm run build'      // react-router build (SSR bundle + client assets)
                }
            }
        }

        stage('Docker: build images') {
            steps {
                sh 'docker compose --profile app build'
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
                expression { params.DEPLOY }
            }
            steps {
                // Secrets come from Jenkins credentials, injected as env vars for compose.
                withCredentials([
                    string(credentialsId: 'lucabridge-jwt-secret', variable: 'JWT_SECRET'),
                    string(credentialsId: 'lucabridge-admin-hash', variable: 'APP_ADMIN_PASSWORD_HASH'),
                    string(credentialsId: 'lucabridge-db-password', variable: 'DB_PASSWORD')
                ]) {
                    sh 'SPRING_PROFILE=prod docker compose --profile app up -d'
                }
                // Simple smoke check: SSR page must answer through nginx.
                sh 'sleep 10 && curl -fsS http://localhost/zh-Hant > /dev/null'
            }
        }
    }

    post {
        failure {
            echo 'Build failed — see stage logs above.'
        }
    }
}
