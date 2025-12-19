# Parent Control API

RESTful API for parental monitoring data synchronization.

## Deployment to Vercel

1. Push this folder to GitHub
2. Import the repository in Vercel
3. Set environment variable: `SECRET_KEY`
4. Deploy

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new device
- `POST /api/auth/login` - Verify device token

### Data Upload (requires X-API-Token header)
- `POST /api/data/location` - Upload location data
- `POST /api/data/contacts` - Upload contacts
- `POST /api/data/sms` - Upload SMS logs
- `POST /api/data/gallery` - Upload gallery metadata

### Data Fetch (requires X-API-Token header)
- `GET /api/fetch/all` - Get all device data
- `GET /api/fetch/locations` - Get location history
- `GET /api/fetch/contacts` - Get contacts
- `GET /api/fetch/sms` - Get SMS logs
- `GET /api/fetch/gallery` - Get gallery metadata

### Public
- `GET /api/devices` - List registered devices

## Security Features
- API token authentication
- Encrypted data transmission (HTTPS)
- Token-based access control
