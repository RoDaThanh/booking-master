-- Insert bookings for reserved/booked seats
INSERT INTO booking (
    id, expires_at, reserved_at, seat_id, payment_id, payment_intent_id, status, user_id
)
VALUES
  (RANDOM_UUID(), TIMESTAMP '2025-04-25 10:30:00', TIMESTAMP '2025-04-25 10:00:00', 3, 'PMT12345', 'INTENT123', 'CONFIRMED', 'user001'),
  (RANDOM_UUID(), TIMESTAMP '2025-04-26 11:30:00', TIMESTAMP '2025-04-26 11:00:00', 4, 'PMT67890', 'INTENT456', 'PENDING', 'user002');
