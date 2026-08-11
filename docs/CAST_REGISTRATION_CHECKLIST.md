# First Nest Hub registration checklist

> **EXTERNAL_BLOCKER:** Google Cast Developer Console identity/payment
> verification currently opens a blank page and prevents developer registration.
> This is not a receiver, Android, build or architecture failure. No code
> workaround or fabricated Application ID is used.

This checklist prepares the first physical test. Completing it does not by
itself verify Cast behavior. Record the actual run in
`test-results/REAL_DEVICE_TEST_TEMPLATE.md`.

Official references checked on 2026-08-11:

- [Google Cast registration](https://developers.google.com/cast/docs/registration)
- [Custom Web Receiver](https://developers.google.com/cast/docs/web_receiver/basic)
- [Chrome Remote Debugger](https://developers.google.com/cast/docs/debugging/remote_debugger)
- [GitHub Pages custom workflows](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages)
- [GitHub Pages publishing source](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site)
- [GitHub Pages HTTPS](https://docs.github.com/en/pages/getting-started-with-github-pages/securing-your-github-pages-site-with-https)
- [Vite GitHub Pages deployment](https://vite.dev/guide/static-deploy.html#github-pages)

## A. Publish the receiver with GitHub Pages

- [ ] Push the repository's `main` branch to `viktorjonsson77/radioapp`.
- [ ] On GitHub, open **Settings → Pages**.
- [ ] Under **Build and deployment → Source**, select **GitHub Actions**. Do
      not select a branch or `/docs`; the checked-in workflow builds Vite.
- [ ] Open **Actions** and confirm that **Deploy Cast receiver to GitHub
      Pages** completes both its `build` and `deploy` jobs. If the path-filtered
      push did not run it, use **Run workflow** on `main`.
- [ ] Open `https://viktorjonsson77.github.io/radioapp/` in a private browser
      window. It must return the RadioApp receiver browser screen over HTTPS.
- [ ] In browser DevTools Network, verify HTTP 200 for `index.html`, the hashed
      JavaScript and CSS, `generated/channels.json`, and
      `assets/radioapp-channel.svg`.
- [ ] Verify sensible response types: HTML, JavaScript, CSS, JSON and SVG. Pages
      supplies standard static MIME types; this check confirms the live site.
- [ ] Confirm that no request incorrectly targets the domain root such as
      `https://viktorjonsson77.github.io/assets/...`; every receiver asset must
      remain below `/radioapp/`.

The workflow builds from source with `npm ci`, tests, then uploads only
`receiver/dist`. `dist`, `node_modules`, APKs and Android build outputs stay out
of Git. Vite uses `base: "/radioapp/"`. Hashed JS/CSS filenames make asset
caching safe; the receiver URL remains stable. After a deployment, wait for the
successful Pages job and hard-refresh before diagnosing stale browser content.

## B. Register the Custom Web Receiver

- [ ] Sign in to the [Google Cast SDK Developer Console](https://cast.google.com/)
      with the account that will own both application and development device.
- [ ] From **Overview** or **Applications**, choose **Add New Application**.
- [ ] Choose **Custom Receiver**.
- [ ] Name it `RadioApp`.
- [ ] Enter this exact Receiver Application URL, including trailing slash:

      `https://viktorjonsson77.github.io/radioapp/`

      The deployed artifact has `index.html` at its root, and GitHub Pages maps
      the project-site URL ending in `/` to that file. A separate SPA route is
      neither used nor required.
- [ ] Enable **Supports casting to audio-only devices** if Nest Audio, Nest Mini
      or other audio-only targets should be discoverable. This does not create a
      browse/touch UI on those devices.
- [ ] Decide **Supports relay casting** deliberately. It is not needed for the
      initial same-network verification.
- [ ] Save and copy the displayed Receiver Application ID.
- [ ] If the console requests Android Sender Details, use package
      `se.radioapp.app`.

Do not publish the Cast application for this private development test. An
unpublished Custom Receiver is accessible to registered development devices.

## C. Register the Nest Hub as a development device

- [ ] Complete normal Google Home setup and put the Hub on the same Wi-Fi as the
      computer used for registration.
- [ ] In the Cast Developer Console, use its Cast button to cast the console tab
      to the target Hub. Google documents that a display device shows and reads
      its Cast serial number aloud. Record that serial number carefully,
      distinguishing `0` from `O`.
- [ ] From **Overview** or **Devices**, choose **Add New Device**.
- [ ] Enter the Cast serial number and a clear description, then save.
- [ ] Wait at least 15 minutes. Continue only when device status reads
      **Ready for Testing**.
- [ ] Restart the Hub by disconnecting and reconnecting power, as Google's
      registration procedure requires after the wait.

## D. Configure and rebuild Android

- [ ] Create or edit `/opt/radioapp/android/local.properties` (Git-ignored):

      ```properties
      sdk.dir=/opt/android-sdk
      CAST_RECEIVER_APP_ID=<id from Cast Developer Console>
      ```

- [ ] Rebuild from `/opt/radioapp/android`:

      ```bash
      ./gradlew clean test lintDebug assembleDebug
      ```

- [ ] Record the new SHA-256:

      ```bash
      sha256sum app/build/outputs/apk/debug/app-debug.apk
      ```

## E. Run the first physical test

- [ ] Put Android and Nest Hub on the same non-isolated Wi-Fi network.
- [ ] Install with
      `adb install -r /opt/radioapp/android/app/build/outputs/apk/debug/app-debug.apk`.
- [ ] Open RadioApp and verify that the Cast button discovers the registered Hub.
- [ ] Connect to the Hub and play P1.
- [ ] Verify audible AAC playback and the Hub's Now Playing metadata.
- [ ] Open in-player Browse on the Hub (Google documents swipe up on smart
      displays) and verify the deterministic 12-channel selected list.
- [ ] Select P3 using Hub touch and verify that playback changes.
- [ ] Close the Android app and verify whether playback continues.
- [ ] While Android remains closed, attempt another Hub-only channel change.
- [ ] Test stop, restart and idle behavior. Record observations; do not infer a
      permanent receiver lifetime from a short run.

## F. Debug failures

- [ ] Launch the receiver from RadioApp first. Application and device must be
      registered to the same Cast developer account.
- [ ] On a computer on the same network, open `chrome://inspect`, locate the Hub
      receiver and choose **Inspect**. Google also documents direct inspection at
      the Hub IP on port `9222`.
- [ ] Capture console entries prefixed `[RadioApp Receiver]`, especially
      `startup`, `ready`, sender connect/disconnect, `media load`, player state
      and stream errors. Do not leave remote debugging attached indefinitely.
- [ ] Capture Android logs without secrets:

      ```bash
      adb logcat -c
      adb logcat | grep -E 'RadioApp|Cast|RemoteMediaClient'
      ```

- [ ] Copy only relevant sanitized excerpts into the real-device test record.
