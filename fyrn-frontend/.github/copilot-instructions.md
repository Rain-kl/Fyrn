# Fyrn Frontend Coding Instructions

## Project Overview
- **Framework**: Nuxt 3 (v4 compatibility mode) with `srcDir: 'app'`.
- **UI Library**: [Una UI](https://unaui.com/) (`@una-ui/nuxt`). Components are prefixed with `N` (e.g., `<NButton>`, `<NInput>`).
- **Styling**: UnoCSS for utility-first styling.
- **API Layer**: Generated OpenAPI client located in `app/api`.

## Architecture & Patterns

### API Communication
- **Always** use the `useApi()` composable from `~/api/useApi` to access backend services.
- Do not use `useFetch` or `$fetch` directly for backend APIs unless there's a specific reason.
- The `useApi()` composable includes built-in error handling and toast notifications via middleware.
- Example:
  ```typescript
  const { JobControllerApi } = useApi()
  const data = await JobControllerApi.listJobs({ page: 1, size: 10 })
  ```

### Component Organization
- Components are located in `app/components/` and organized by module:
  - `oms/`: Operator Management System components.
  - `mms/`: Material/Novel Management System components.
  - `common/`: Reusable generic components.
  - `app/`: Layout-related components (Header, Footer, Logo).

### Data Tables
- Use `@tanstack/vue-table` for complex data tables.
- Refer to `app/components/oms/JobListManagement.vue` for a standard implementation of filtering, pagination, and status badges.

### State Management
- Use Vue's `ref` and `reactive` for local state.
- Use Nuxt's `useState` or `pinia` (if added) for global state.

## Developer Workflows
- **Development**: `pnpm dev`
- **Build**: `pnpm build`
- **Linting**: Follow existing ESLint/Prettier configurations.

## Conventions
- **Naming**: Use PascalCase for components and camelCase for variables/functions.
- **Icons**: Use UnoCSS icon classes (e.g., `i-lucide-list-checks`).
- **Date Formatting**: Use utilities in `app/utils/date.ts` (e.g., `formatToYMDHMS`).
- **Type Safety**: Always use generated models from `~/api/models` for DTOs and API responses.

## Key Files
- `nuxt.config.ts`: Nuxt configuration and proxy settings.
- `app/api/useApi.ts`: API client initialization and middleware.
- `uno.config.ts`: UnoCSS and Una UI theme configuration.
