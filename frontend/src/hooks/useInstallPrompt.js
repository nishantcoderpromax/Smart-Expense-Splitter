import { useEffect, useState } from "react";

/**
 * Browsers fire 'beforeinstallprompt' and then wait — they don't show their
 * own install UI unless the app calls prompt() on that captured event. This
 * hook captures it once and exposes a promptInstall() function plus whether
 * installation is currently possible (already-installed browsers/PWAs never
 * fire the event again, so the button naturally disappears after install).
 */
export function useInstallPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [installed, setInstalled] = useState(false);

  useEffect(() => {
    const handleBeforeInstall = (e) => {
      e.preventDefault();
      setDeferredPrompt(e);
    };
    const handleInstalled = () => {
      setInstalled(true);
      setDeferredPrompt(null);
    };

    window.addEventListener("beforeinstallprompt", handleBeforeInstall);
    window.addEventListener("appinstalled", handleInstalled);
    return () => {
      window.removeEventListener("beforeinstallprompt", handleBeforeInstall);
      window.removeEventListener("appinstalled", handleInstalled);
    };
  }, []);

  const promptInstall = async () => {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    await deferredPrompt.userChoice;
    setDeferredPrompt(null); // the captured event can only be used once
  };

  return { canInstall: !!deferredPrompt && !installed, promptInstall };
}
