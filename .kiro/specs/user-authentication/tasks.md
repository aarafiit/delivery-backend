# Implementation Plan: User Authentication

## Overview

This plan implements a minimal user authentication system with registration and login endpoints. The implementation uses Java with a three-layer architecture: API layer (REST endpoints), Service layer (business logic), and Data layer (database persistence). Tasks are ordered to build incrementally, validating core functionality early.

## Tasks

- [x] 1. Set up project structure and data models
  - Create User entity with id, fullName, username, email, passwordHash, createdAt fields
  - Create RegistrationRequest and LoginRequest DTOs
  - Create UserResponse DTO (excludes passwordHash)
  - Add database configuration and connection setup
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 2. Implement password hashing service
  - [x] 2.1 Create PasswordHashingService with hashPassword and verifyPassword methods
    - Use BCrypt for secure password hashing
    - _Requirements: 6.1, 6.2_
  
  - [ ]* 2.2 Write property test for password hashing
    - **Property 3: Password Hashing Round-Trip**
    - **Validates: Requirements 1.4, 2.4, 6.1, 6.2**

- [ ] 3. Implement validation service
  - [x] 3.1 Create ValidationService with input validation methods
    - Implement validateRegistrationInput (check empty fields, email format, password length >= 6)
    - Implement validateLoginInput (check empty username and password)
    - Implement validateEmailFormat (basic email regex)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 5.1, 5.2_
  
  - [ ]* 3.2 Write property tests for validation
    - **Property 8: Empty Field Validation for Registration**
    - **Property 9: Invalid Email Format Validation**
    - **Property 10: Password Length Validation**
    - **Property 11: Empty Field Validation for Login**
    - **Validates: Requirements 4.1-4.6, 5.1, 5.2**

- [ ] 4. Implement data layer
  - [x] 4.1 Create UserRepository interface and implementation
    - Implement createUser, findByUsername, findByEmail methods
    - Implement existsByUsername and existsByEmail methods
    - Add database constraints for unique username and email
    - _Requirements: 1.2, 1.3, 1.5_
  
  - [ ]* 4.2 Write unit tests for UserRepository
    - Test user creation and retrieval
    - Test uniqueness constraints
    - _Requirements: 1.2, 1.3, 1.5_

- [x] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implement registration service and endpoint
  - [x] 6.1 Create RegistrationService
    - Implement registerUser method with validation, uniqueness checks, password hashing, and user creation
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  
  - [x] 6.2 Create POST /register endpoint
    - Parse RegistrationRequest from JSON
    - Call RegistrationService
    - Return 201 with userId on success, 400 on validation/conflict errors
    - _Requirements: 2.1, 2.2, 2.3, 2.5_
  
  - [ ]* 6.3 Write property tests for registration
    - **Property 1: Username Uniqueness**
    - **Property 2: Email Uniqueness**
    - **Property 4: Successful Registration**
    - **Validates: Requirements 1.2, 1.3, 2.1, 2.2, 2.3, 2.5**

- [ ] 7. Implement login service and endpoint
  - [x] 7.1 Create LoginService
    - Implement authenticateUser method with validation, user lookup, and password verification
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [x] 7.2 Create POST /login endpoint
    - Parse LoginRequest from JSON
    - Call LoginService
    - Return 200 with UserResponse on success, 401 on auth failure, 400 on validation error
    - _Requirements: 3.1, 3.5_
  
  - [ ]* 7.3 Write property tests for login
    - **Property 5: Non-Existent Username Login Failure**
    - **Property 6: Incorrect Password Login Failure**
    - **Property 7: Successful Login**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.5**

- [x] 8. Implement error handling
  - Add exception handling for validation errors (400), conflicts (400), authentication failures (401), and server errors (500)
  - Ensure error responses follow format: {"success": false, "error": "message"}
  - Ensure success responses follow format: {"success": true, ...}
  - _Requirements: All requirements (error handling)_

- [x] 9. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties with minimum 100 iterations
- Property test comments must include: `Feature: user-authentication, Property {number}: {property_text}`
- Password hashing uses BCrypt for security
- Database constraints enforce username and email uniqueness
- Error responses never distinguish between non-existent username and incorrect password for security
