import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { adminApi, setToken, getToken } from "../../lib/adminApi";
import adminStyles from "../../theme/admin.css?url";

export const links = () => [{ rel: "stylesheet", href: adminStyles }];
export const meta = () => [{ title: "Sign in · LucaBridge CMS" }];

export default function AdminLogin() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (getToken()) navigate("/admin", { replace: true });
  }, [navigate]);

  async function onSubmit(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      const res = await adminApi.login(username.trim(), password);
      setToken(res.token);
      navigate("/admin", { replace: true });
    } catch (err) {
      setError(err.message || "Login failed");
      setBusy(false);
    }
  }

  return (
    <div className="admin-login">
      <form className="admin-login-card" onSubmit={onSubmit}>
        <h1>LucaBridge <span>CMS</span></h1>
        <p className="sub">Sign in to manage the site</p>
        {error ? <div className="admin-alert error">{error}</div> : null}
        <div className="admin-field">
          <label htmlFor="u">Username</label>
          <input id="u" value={username} onChange={(e) => setUsername(e.target.value)}
                 autoFocus autoComplete="username" />
        </div>
        <div className="admin-field">
          <label htmlFor="p">Password</label>
          <input id="p" type="password" value={password} onChange={(e) => setPassword(e.target.value)}
                 autoComplete="current-password" />
        </div>
        <button className="admin-btn primary" type="submit" disabled={busy} style={{ width: "100%", justifyContent: "center" }}>
          {busy ? "Signing in…" : "Sign in"}
        </button>
      </form>
    </div>
  );
}
