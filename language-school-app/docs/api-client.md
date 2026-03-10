# API-клиент (OpenAPI Generator)

## Источник спецификации

Файл **api-docs.json** в корне проекта `language-school-app`. После изменения спецификации нужно перегенерировать клиент.

## Регенерация клиента

```bash
npm run api:generate
```

Папка **src/app/api** перезаписывается при генерации; не редактируйте файлы в ней вручную.

## Базовый URL

По умолчанию используется URL из api-docs.json (`servers[0].url`). Чтобы переопределить его для локальной разработки, можно предоставить провайдер `BASE_PATH` в `app.config.ts`:

```ts
import { BASE_PATH } from './api/variables';

providers: [
  { provide: BASE_PATH, useValue: 'http://localhost:8080' },
  // ...
]
```

## JWT

После успешного логина через `AuthControllerService.login()` сохраните токен из ответа в `AuthTokenService`:

```ts
this.authApi.login({ email, password }).subscribe((res) => {
  this.authTokenService.setToken(res.token);
});
```

При логауте вызовите `authTokenService.setToken(null)`.

Интерцептор `authInterceptor` автоматически добавляет заголовок `Authorization: Bearer <token>` ко всем запросам, если токен задан.
