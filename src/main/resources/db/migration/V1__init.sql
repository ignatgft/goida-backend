CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,
    full_name VARCHAR(160) NOT NULL,
    avatar_url VARCHAR(512),
    base_currency VARCHAR(3) NOT NULL,
    monthly_budget NUMERIC(19, 2) NOT NULL DEFAULT 0,
    auth_provider VARCHAR(32) NOT NULL,
    google_subject VARCHAR(255) NOT NULL UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE assets (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(32) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    balance NUMERIC(19, 8) NOT NULL DEFAULT 0,
    note VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_assets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_assets_user_id ON assets(user_id);

CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    source_asset_id VARCHAR(36),
    source_asset_name VARCHAR(120),
    title VARCHAR(160),
    category VARCHAR(64) NOT NULL,
    kind VARCHAR(32) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    amount NUMERIC(19, 8) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    note VARCHAR(500),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_asset FOREIGN KEY (source_asset_id) REFERENCES assets(id) ON DELETE SET NULL
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_user_occurred_at ON transactions(user_id, occurred_at);
CREATE INDEX idx_transactions_user_category ON transactions(user_id, category);

CREATE TABLE receipts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(36) UNIQUE,
    merchant VARCHAR(160),
    total NUMERIC(19, 8),
    currency VARCHAR(10),
    purchased_at TIMESTAMP WITH TIME ZONE,
    image_url VARCHAR(512),
    provider_name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_receipts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_receipts_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE SET NULL
);

CREATE INDEX idx_receipts_user_id ON receipts(user_id);
CREATE INDEX idx_receipts_transaction_id ON receipts(transaction_id);

CREATE TABLE receipt_items (
    id VARCHAR(36) PRIMARY KEY,
    receipt_id VARCHAR(36) NOT NULL,
    name VARCHAR(160) NOT NULL,
    quantity NUMERIC(19, 8),
    unit_price NUMERIC(19, 8),
    total_price NUMERIC(19, 8),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_receipt_items_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(id) ON DELETE CASCADE
);

CREATE TABLE chat_history (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    response VARCHAR(4000) NOT NULL,
    provider VARCHAR(64) NOT NULL,
    model VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_chat_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_history_user_id ON chat_history(user_id);
