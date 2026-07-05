-- =====================================================================
-- NHCS — Reset database to a clean slate (REAL-USE mode)
--
-- Removes ALL existing demo/mock data (Rahim Islam, judge accounts,
-- mock doctors, mock hospitals, seeded appointments/prescriptions/
-- lab & imaging reports — i.e. everything that fed the Health Timeline).
--
-- After running this, restart the backend. Hibernate (ddl-auto: update)
-- recreates all empty tables, and DataInitializer seeds ONLY the
-- bootstrap admin account (admin / password123).
--
-- HOW TO RUN (from a terminal):
--   psql -h localhost -U root -d nhcs_db -f reset_to_clean_slate.sql
--   (password: Quanfey)
--
-- WARNING: This deletes everything in the schema. Only run if you truly
-- want a fresh start.
-- =====================================================================

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO root;
GRANT ALL ON SCHEMA public TO public;
