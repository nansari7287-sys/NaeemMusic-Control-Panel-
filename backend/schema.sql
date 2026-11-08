CREATE TABLE IF NOT EXISTS app_config (
 id INTEGER PRIMARY KEY CHECK(id=1), version INTEGER NOT NULL DEFAULT 1,
 config_json TEXT NOT NULL, updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS config_history (
 id INTEGER PRIMARY KEY AUTOINCREMENT, version INTEGER NOT NULL,
 config_json TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT OR IGNORE INTO app_config(id,version,config_json) VALUES(1,1,'{
 "branding":{"appName":"Naeem Music","subtitle":"DrakoXNaeem","primaryColor":"#8B5CF6"},
 "background":{"type":"image","url":"","enabled":false,"autoplay":true,"muted":true,"loop":true},
 "announcement":{"enabled":false,"title":"","message":"","url":""},
 "features":{"lyrics":true,"visualizer":true,"aiSearch":false,"experimentalUi":false},
 "ads":{"enabled":false,"banner":false,"interstitial":false,"native":false,"frequency":0}
}');
