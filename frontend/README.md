# Graph Visualizer — frontend

React + Vite + Cytoscape UI for the Spring Boot API in the parent folder.

```bash
# 1. backend (from the repo root)
./gradlew bootRun          # http://localhost:8080

# 2. frontend
cd frontend
npm install
npm run dev                # http://localhost:3000
```

Port 3000 matters: it's the origin the backend's CORS config allows. To point at a
different API, copy `.env.example` to `.env.local` and set `VITE_API_URL`.

On startup the app loads `GET /api/sample`; if the backend is down it falls back to
the same graph hardcoded in `src/data/sampleGraph.js`.

| Script          | What it does              |
| --------------- | ------------------------- |
| `npm run dev`   | Dev server with HMR       |
| `npm test`      | Unit tests (Vitest)       |
| `npm run lint`  | Oxlint                    |
| `npm run build` | Production build to dist/ |

## Layout

- `src/App.jsx` — all state: graph, selected algorithm, run result, playback
- `src/components/` — `Visualizer` (Cytoscape canvas), `AlgorithmSelector`,
  `Controls`, `GraphEditor`, `StatsPanel`
- `src/services/api.js` — backend calls
- `src/lib/stepState.js` — replays the first N steps into node/edge colors
