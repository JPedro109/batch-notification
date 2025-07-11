INSERT INTO email_templates (name, subject, body_html, version, active, variables)
VALUES
  ('PROMOTION', 'Take advantage of our special promotion!', '<h1>Hello, {{name}}!</h1><p>Visit our website for details {{link}}</p>', 1, true, ARRAY['name', 'link']);

INSERT INTO sms_templates (name, content, version, active, variables)
VALUES
  ('PROMOTION', 'Hi {{name}}! Take advantage of our special promotion! Visit our website for details {{link}}', 1, true, ARRAY['name', 'link']);
