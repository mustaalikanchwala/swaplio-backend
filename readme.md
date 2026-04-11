# Swaplio Backend

A REST API backend for Swaplio — a student marketplace for buying and selling 
second-hand study materials.

---

## Tech Stack

- Java 21 / Spring Boot 4.0.4
- PostgreSQL (Supabase)
- Supabase Storage (S3-compatible)
- JWT Authentication
- Docker + Render deployment

---

## Getting Started Locally

### Prerequisites
- Java 21+
- Maven
- IntelliJ IDEA
- Supabase account

### Setup

1. Clone the repo
```bash
git clone https://github.com/yourusername/swaplio-backend.git
cd swaplio-backend
```

2. Create `.env` file in project root:
```env
DB_URL=jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres?user=postgres.xxxxx&password=xxxxx&sslmode=require
DB_USERNAME=postgres.xxxxx
DB_PASSWORD=your-password
JWT_SECRET=your-secret-minimum-32-chars
JWT_EXPIRATION=86400000
SUPABASE_STORAGE_URL=https://xxxxx.supabase.co/storage/v1/s3
SUPABASE_ACCESS_KEY=your-access-key
SUPABASE_SECRET_KEY=your-secret-key
SUPABASE_BUCKET=listing-images
SUPABASE_REGION=ap-south-1
SUPABASE_PROJECT_URL=https://xxxxx.supabase.co
```

3. Install EnvFile plugin in IntelliJ → link `.env` in Run Configuration

4. Run the app — it starts on `http://localhost:8080`

---

## API Documentation

After running, open: `http://localhost:8080/swagger-ui.html`

### Base URL
- Local: `http://localhost:8080`
- Production: `https://swaplio-backend.onrender.com`

### Authentication
All protected endpoints require:
```
Header: Authorization: Bearer {token}
```
Get the token from `/api/auth/login`.

---

## API Endpoints

### Auth
| Method | URL | Auth | Description |
|--------|-----|:----:|-------------|
| POST | `/api/auth/register` | No | Register new user |
| POST | `/api/auth/login` | No | Login, get JWT token |

### Users
| Method | URL | Auth | Description |
|--------|-----|:----:|-------------|
| GET | `/api/users/me` | Yes | Get my profile |
| PUT | `/api/users/me` | Yes | Update my profile |

### Listings
| Method | URL | Auth | Description |
|--------|-----|:----:|-------------|
| GET | `/api/listings` | No | All listings (paginated) |
| GET | `/api/listings/{id}` | No | Single listing |
| GET | `/api/listings/my` | Yes | My own listings |
| GET | `/api/listings/search` | No | Search and filter |
| POST | `/api/listings` | Yes | Create listing + images |
| PUT | `/api/listings/{id}` | Yes | Update listing + images |
| PATCH | `/api/listings/{id}/sold` | Yes | Mark as sold |
| DELETE | `/api/listings/{id}` | Yes | Delete listing |

### Categories
| Method | URL | Auth | Description |
|--------|-----|:----:|-------------|
| GET | `/api/categories` | No | All categories |

### Meetings
| Method | URL | Auth | Description |
|--------|-----|:----:|-------------|
| POST | `/api/meetings` | Yes | Schedule meeting |
| GET | `/api/meetings/my/buying` | Yes | My meetings as buyer |
| GET | `/api/meetings/my/selling` | Yes | My meetings as seller |

---

## Image Upload Rules

- Images sent as `multipart/form-data` together with listing data
- Max 5 images per listing, max 10MB per file
- First image is automatically set as primary (thumbnail)
- On update: send `keepImageIds` for existing images to keep
- Images stored in Supabase Storage (private bucket)
- Signed URLs generated on every response (valid 1 hour)

---

## Deployment

### Docker
```bash
docker build -t swaplio-backend .
docker run -p 8080:8080 --env-file .env swaplio-backend
```

### Render
- Connected to GitHub main branch
- Auto-deploys on every push
- Environment variables set in Render dashboard

---

## Project Structure

```
src/main/java/com/swaplio/
├── config/         SecurityConfig, SwaggerConfig, SupabaseStorageConfig
├── controller/     AuthController, ListingController, UserController,
│                   CategoryController, MeetingController
├── dto/            auth/, listing/, user/, meeting/
├── entity/         User, Listing, ListingImage, Category, Meeting
├── exception/      GlobalExceptionHandler
├── filter/         JwtAuthFilter
├── repository/     UserRepository, ListingRepository,
│                   ListingImageRepository, CategoryRepository,
│                   MeetingRepository
├── service/        AuthService, ListingService, UserService,
│                   StorageService, MeetingService
└── util/           JwtUtil
```

---

## Error Response Format

All errors return consistent JSON:
```json
{
  "timestamp": "2026-04-06T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Listing not found"
}
```

---

## Environment Variables Reference

| Variable | Description |
|----------|-------------|
| DB_URL | Supabase JDBC connection string |
| DB_USERNAME | postgres.{projectRef} |
| DB_PASSWORD | Supabase DB password |
| JWT_SECRET | Min 32 character secret string |
| JWT_EXPIRATION | Token expiry in ms (86400000 = 24hrs) |
| SUPABASE_STORAGE_URL | Supabase S3 endpoint |
| SUPABASE_ACCESS_KEY | Supabase Storage access key |
| SUPABASE_SECRET_KEY | Supabase Storage secret key |
| SUPABASE_BUCKET | Storage bucket name |
| SUPABASE_REGION | ap-south-1 |
| SUPABASE_PROJECT_URL | https://{projectRef}.supabase.co |