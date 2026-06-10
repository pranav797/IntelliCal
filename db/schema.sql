
-- USERS
CREATE TABLE IF NOT EXISTS users (
    id              SERIAL          PRIMARY KEY,
    username        TEXT            NOT NULL,
    email           TEXT            NOT NULL UNIQUE,
    member_since    TEXT            NOT NULL,
    target_date     TEXT            NOT NULL,
    current_weight  REAL            NOT NULL,
    target_weight   REAL            NOT NULL,
    start_weight    REAL            NOT NULL,
    primary_goal    TEXT            NOT NULL CHECK (primary_goal IN ('WEIGHT_LOSS', 'MUSCLE_GAIN', 'CLEAN_EATING')),
    streak          INTEGER         NOT NULL DEFAULT 0,
    days_tracked    INTEGER         NOT NULL DEFAULT 0,
    meals_logged    INTEGER         NOT NULL DEFAULT 0
);

-- MEALS
CREATE TABLE IF NOT EXISTS meals (
    id              TEXT            PRIMARY KEY,
    user_id         INTEGER         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            TEXT            NOT NULL,
    calories        INTEGER         NOT NULL,
    protein_grams   INTEGER         NOT NULL,
    carbs_grams     INTEGER         NOT NULL,
    fat_grams       INTEGER         NOT NULL,
    health_score    INTEGER         NOT NULL CHECK (health_score BETWEEN 0 AND 10),
    timestamp       BIGINT          NOT NULL,
    ingredients     JSONB           NOT NULL DEFAULT '[]'
);

-- FORESTS
CREATE TABLE IF NOT EXISTS forests (
    user_id         INTEGER         PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    total_xp        INTEGER         NOT NULL DEFAULT 0,
    streak_days     INTEGER         NOT NULL DEFAULT 0,
    grid_size       INTEGER         NOT NULL DEFAULT 5
);

-- TREES
CREATE TABLE IF NOT EXISTS trees (
    id                          SERIAL      PRIMARY KEY,
    user_id                     INTEGER     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    row_pos                     INTEGER     NOT NULL,
    col_pos                     INTEGER     NOT NULL,
    type                        TEXT        NOT NULL,
    growth_stage                TEXT        NOT NULL CHECK (growth_stage IN ('SAPLING', 'JUVENILE', 'MATURE')),
    health_state                TEXT        NOT NULL CHECK (health_state IN ('THRIVING', 'HEALTHY', 'STRUGGLING', 'WITHERING')),
    planted_date                BIGINT,
    growth_points               INTEGER     NOT NULL DEFAULT 0,
    consecutive_healthy_days    INTEGER     NOT NULL DEFAULT 0,
    UNIQUE (user_id, row_pos, col_pos)
);

-- NUTRITION GOALS
CREATE TABLE IF NOT EXISTS nutrition_goals (
    user_id         INTEGER         PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    daily_calories  INTEGER         NOT NULL,
    protein_grams   INTEGER         NOT NULL,
    carbs_grams     INTEGER         NOT NULL,
    fat_grams       INTEGER         NOT NULL,
    distribution    TEXT            NOT NULL CHECK (distribution IN ('BALANCED', 'LOW_CARB', 'LOW_FAT'))
);

-- ACHIEVEMENTS
CREATE TABLE IF NOT EXISTS achievements (
    id              SERIAL          PRIMARY KEY,
    user_id         INTEGER         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            TEXT            NOT NULL,
    description     TEXT            NOT NULL,
    icon            TEXT            NOT NULL,
    unlocked        BOOLEAN         NOT NULL DEFAULT FALSE,
    category        TEXT            NOT NULL CHECK (category IN ('NUTRITION', 'FOREST', 'STREAK', 'MILESTONE')),
    required_value  INTEGER         NOT NULL DEFAULT 0,
    current_value   INTEGER         NOT NULL DEFAULT 0
);
