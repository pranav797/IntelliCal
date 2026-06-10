
-- Generic sample user. This does not create a Supabase Auth account.
INSERT INTO users (id, username, email, member_since, target_date, current_weight, target_weight, start_weight, primary_goal, streak, days_tracked, meals_logged)
VALUES (1, 'Demo User', 'demo@example.com', 'Member since Jan 2026', 'June 1, 2026', 170.0, 165.0, 175.0, 'WEIGHT_LOSS', 5, 47, 187)
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- 1 NUTRITION GOAL
INSERT INTO nutrition_goals (user_id, daily_calories, protein_grams, carbs_grams, fat_grams, distribution)
VALUES (1, 2500, 125, 281, 97, 'BALANCED')
ON CONFLICT (user_id) DO NOTHING;

-- 5 MEALS
INSERT INTO meals (id, user_id, name, calories, protein_grams, carbs_grams, fat_grams, health_score, timestamp, ingredients)
VALUES
    ('1', 1, 'Apple Salmon Salad',        500, 78, 78, 78, 9,
     (EXTRACT(EPOCH FROM NOW()) * 1000 - 7200000)::BIGINT,
     '["Salmon","Apple","Mixed Greens","Olive Oil"]'),

    ('2', 1, 'Greek Yogurt with Berries', 320, 25, 45,  8, 8,
     (EXTRACT(EPOCH FROM NOW()) * 1000 - 18000000)::BIGINT,
     '["Greek Yogurt","Blueberries","Strawberries","Honey"]'),

    ('3', 1, 'Grilled Chicken Wrap',      450, 35, 40, 15, 7,
     (EXTRACT(EPOCH FROM NOW()) * 1000 - 28800000)::BIGINT,
     '["Chicken Breast","Tortilla","Lettuce","Tomato"]'),

    ('4', 1, 'Oatmeal with Banana',       280,  8, 52,  5, 7,
     (EXTRACT(EPOCH FROM NOW()) * 1000 - 36000000)::BIGINT,
     '["Oats","Banana","Almond Milk","Cinnamon"]'),

    ('5', 1, 'Protein Smoothie',          350, 30, 40, 10, 8,
     (EXTRACT(EPOCH FROM NOW()) * 1000 - 43200000)::BIGINT,
     '["Whey Protein","Banana","Peanut Butter","Milk"]')
ON CONFLICT (id) DO NOTHING;

-- 1 FOREST
INSERT INTO forests (user_id, total_xp, streak_days, grid_size)
VALUES (1, 2095, 5, 5)
ON CONFLICT (user_id) DO NOTHING;

-- 5 TREES
INSERT INTO trees (user_id, row_pos, col_pos, type, growth_stage, health_state, planted_date, growth_points, consecutive_healthy_days)
VALUES
    (1, 1, 2, 'Oak',    'MATURE',   'THRIVING', (EXTRACT(EPOCH FROM NOW()) * 1000 - 1036800000)::BIGINT, 150, 12),
    (1, 0, 4, 'Willow', 'MATURE',   'HEALTHY',  (EXTRACT(EPOCH FROM NOW()) * 1000 -  777600000)::BIGINT, 110,  9),
    (1, 3, 1, 'Bamboo', 'JUVENILE', 'THRIVING', (EXTRACT(EPOCH FROM NOW()) * 1000 -  432000000)::BIGINT,  60,  5),
    (1, 2, 3, 'Cactus', 'SAPLING',  'HEALTHY',  (EXTRACT(EPOCH FROM NOW()) * 1000 -  172800000)::BIGINT,  20,  2),
    (1, 4, 0, 'Birch',  'JUVENILE', 'HEALTHY',  (EXTRACT(EPOCH FROM NOW()) * 1000 -  518400000)::BIGINT,  70,  6)
ON CONFLICT (user_id, row_pos, col_pos) DO NOTHING;

-- 8 ACHIEVEMENTS
INSERT INTO achievements (id, user_id, name, description, icon, unlocked, category, required_value, current_value)
VALUES
    (1, 1, 'First Meal',    'Log your first meal',                     '🍽️', TRUE,  'NUTRITION',  1,   1),
    (2, 1, 'Week Streak',   'Maintain a 7-day streak',                 '🔥', TRUE,  'STREAK',     7,   7),
    (3, 1, 'Forest Keeper', 'Plant 5 trees in your forest',           '🌲', TRUE,  'FOREST',     5,   5),
    (4, 1, '10 Trees',      'Plant 10 trees total',                    '🌳', TRUE,  'FOREST',    10,  10),
    (5, 1, '100 Meals',     'Log 100 meals',                           '💯', TRUE,  'NUTRITION', 100, 100),
    (6, 1, '21 Days',       'Maintain a 21-day streak',                '📅', FALSE, 'STREAK',    21,   5),
    (7, 1, 'Ancient Tree',  'Grow a tree to Mature stage',             '🏆', FALSE, 'FOREST',     1,   0),
    (8, 1, 'Perfect Week',  'Hit all macro goals for 7 days straight', '⭐', FALSE, 'MILESTONE',  7,   2)
ON CONFLICT (id) DO NOTHING;

SELECT setval('achievements_id_seq', (SELECT MAX(id) FROM achievements));
