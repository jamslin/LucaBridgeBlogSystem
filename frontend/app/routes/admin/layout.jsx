import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router";
import { adminApi, getToken, clearToken } from "../../lib/adminApi";
import adminStyles from "../../theme/admin.css?url";

export const links = () => [{ rel: "stylesheet", href: adminStyles }];
export const meta = () => [{ title: "LucaBridge CMS" }];

const NAV = [
  { to: "/admin", label: "Dashboard", end: true },
  { to: "/admin/posts", label: "Posts" },
  { to: "/admin/events", label: "Events" },
  { to: "/admin/jobs", label: "Jobs" },
  { to: "/admin/pages", label: "Pages" },
  { to: "/admin/categories", label: "Categories" },
  { to: "/admin/media", label: "Media" },
  { to: "/admin/settings", label: "Settings" },
  { to: "/admin/users", label: "Users", adminOnly: true },
];

export default function AdminLayout() {
  const navigate = useNavigate();
  const [ready, setReady] = useState(false);
  const [user, setUser] = useState(null);

  useEffect(() => {
    if (!getToken()) {
      navigate("/admin/login", { replace: true });
      return;
    }
    adminApi.me()
      .then((me) => { setUser(me); setReady(true); })
      .catch(() => { clearToken(); navigate("/admin/login", { replace: true }); });
  }, [navigate]);

  function logout() {
    clearToken();
    navigate("/admin/login", { replace: true });
  }

  // Rendered identically on the server and the first client paint (no token
  // access during render), so hydration matches; the effect then gates access.
  if (!ready || !user) {
    return <div className="admin-loading">Loading…</div>;
  }

  const isAdmin = user.roles.includes("ADMIN");
  const items = NAV.filter((i) => !i.adminOnly || isAdmin);

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand">Luca<span>Bridge</span></div>
        <nav className="admin-nav">
          {items.map((i) => (
            <NavLink key={i.to} to={i.to} end={i.end}
                     className={({ isActive }) => (isActive ? "active" : undefined)}>
              {i.label}
            </NavLink>
          ))}
        </nav>
        <div className="admin-sidebar-foot">
          <div className="who">
            {user.username}
            <small>{user.roles.join(", ")}</small>
          </div>
          <button className="admin-btn admin-btn-sm ghost" onClick={logout}
                  style={{ color: "#d9d1c4", paddingLeft: 0 }}>
            Sign out
          </button>
        </div>
      </aside>
      <div className="admin-main">
        <Outlet context={{ user, isAdmin }} />
      </div>
    </div>
  );
}
