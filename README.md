# didactic-octo-fishstick

## How to test ?
After pipeline pass everything.
TAG=[branch-name] docker compose up

if main branch, use : docker compose up
Then go to : http://localhost/

# Didactic Octo Fishstick - Frontend

This is the frontend part of the **Didactic Octo Fishstick** project, a photo/game search platform built with Angular.

## 🚀 Technologies

- **Framework**: [Angular](https://angular.io/)
- **Styling**: [Bootstrap 5](https://getbootstrap.com/)
- **Testing**: [Vitest](https://vitest.dev/) & [Cypress](https://www.cypress.io/)
- **State Management**: RxJS

## 🛠️ Development Setup

### Prerequisites

- [Node.js](https://nodejs.org/) (latest LTS recommended)
- [npm](https://www.npmjs.com/)

### Installation

1.  Navigate to the frontend directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```

### Running Locally

To start the development server:
```bash
npm start
```
The application will be available at `http://localhost:4200/`.

### Building

To build the project for production:
```bash
npm run build
```
The build artifacts will be stored in the `dist/` directory.

## 🧪 Testing

### Unit Tests
```bash
npm test
```

### End-to-End (E2E) Tests
```bash
npx cypress open
```

## 📂 Project Structure

- `src/app/`: Contains the main Angular components, services, and modules.
  - `admin-page/`: Admin dashboard for managing games and users.
  - `home-page/`: Main landing page.
  - `search-page/`: Search functionality for games.
  - `image-list/`: Component for displaying lists of games/images.
- `src/assets/`: Static assets like images and icons.
- `cypress/`: E2E testing files.

## 🐳 How to test with Docker?

After the pipeline passes:
```bash
TAG=sha-abc1234 docker compose up -d
```
If you don't know the sha : 
``` bash
git log --oneline
```

If you are on the `main` branch, use:
```bash
TAG=main docker compose up -d
```
Then go to: `http://localhost/`
