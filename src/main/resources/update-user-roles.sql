-- Update existing users to have default role
-- This will fix the 403 Forbidden error for existing users

UPDATE users 
SET role = 'USER' 
WHERE role IS NULL OR role = '';

-- Verify the update
SELECT id, email, role FROM users;
