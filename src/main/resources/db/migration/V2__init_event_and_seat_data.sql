-- Insert sample events
INSERT INTO event (price, total_seats, date, name)
VALUES
  (49.99, 100, TIMESTAMP '2025-05-01 19:00:00', 'Rock Concert'),
  (29.99, 50, TIMESTAMP '2025-05-05 18:30:00', 'Comedy Night');

-- Insert sample seats for both events
INSERT INTO seat (event_id, version, seat_number, status)
VALUES
  (1, 1, 'A1', 'AVAILABLE'),
  (1, 1, 'A2', 'AVAILABLE'),
  (1, 1, 'A3', 'RESERVED'),
  (2, 1, 'B1', 'BOOKED'),
  (2, 1, 'B2', 'AVAILABLE');
