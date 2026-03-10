export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = {
  token: string;
};

export type LoginResult = {
  token: string | null;
  redirectPath: string;
};
