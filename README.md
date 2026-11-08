# Naeem Music Control Panel

A separate web dashboard and Android admin app for remotely managing Naeem Music.

## Architecture
- `web/`: browser dashboard
- `backend/`: Cloudflare Worker API, D1 database and R2 media storage
- `android/`: separate Android admin client (next implementation stage)
- Existing Naeem Music app: receives safe, validated remote configuration after its client integration.

## Backend setup
1. Install Node.js 20+ and Wrangler: `npm install -g wrangler`
2. Run `wrangler login`
3. In backend, create D1: `wrangler d1 create naeem-music-control`; paste its ID into `wrangler.toml`
4. Create R2 bucket: `wrangler r2 bucket create naeem-music-media`
5. Run `npm install` then `wrangler d1 execute naeem-music-control --remote --file=./schema.sql`
6. Set secret: `wrangler secret put ADMIN_TOKEN` (use a long random value; never commit it)
7. Configure R2 public custom domain and set `PUBLIC_MEDIA_BASE_URL`
8. Deploy: `npm run deploy`

## Web setup
Set `VITE_API_BASE` in `web/.env` to the deployed Worker URL, then `npm install && npm run dev`.

## Important
The existing Naeem Music APK needs a one-time Remote Config client integration and a new APK install. Remote config can update only settings the app already knows how to render. Launcher icon/label and APK upgrades still require an APK update. Ads controls require an ad SDK already integrated and policy-compliant. The app must cache last-known-good config and use bundled defaults offline. No remote code execution or arbitrary device commands.
