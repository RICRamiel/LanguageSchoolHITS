import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';

export type AdminLanguageDTO = { name?: string; id?: number };
export type AdminGroupDTO = {
  id?: number;
  name?: string;
  language?: { name?: string };
};
export type AdminUserDTO = {
  id?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  groups?: Array<{ id?: number; name?: string }>;
};

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  private normalizeArray<T>(payload: unknown): T[] {
    if (Array.isArray(payload)) return payload as T[];
    if (payload && typeof payload === 'object') {
      const o = payload as Record<string, unknown>;
      if (Array.isArray(o['content'])) return o['content'] as T[];
      if (Array.isArray(o['data'])) return o['data'] as T[];
      if (Array.isArray(o['body'])) return o['body'] as T[];
    }
    return [];
  }

  getLanguages(): Observable<AdminLanguageDTO[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.admin.languages.list))
      .pipe(
        map((res) => this.normalizeArray<AdminLanguageDTO>(res)),
        catchError(() => of([])),
      );
  }

  createLanguage(name: string): Observable<AdminLanguageDTO> {
    return this.http.post<AdminLanguageDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.languages.create),
      { name },
    );
  }

  editLanguage(id: number, name: string): Observable<AdminLanguageDTO> {
    return this.http.put<AdminLanguageDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.languages.edit(id)),
      { name },
    );
  }

  deleteLanguage(id: number): Observable<void> {
    return this.http
      .delete(withOpenApiBase(OPENAPI_PATHS.admin.languages.delete(id)))
      .pipe(map(() => undefined));
  }

  getGroups(): Observable<AdminGroupDTO[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.admin.groups.list))
      .pipe(
        map((res) => this.normalizeArray<AdminGroupDTO>(res)),
        catchError(() => of([])),
      );
  }

  createGroup(name: string, languageName: string): Observable<AdminGroupDTO> {
    return this.http.post<AdminGroupDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.groups.create),
      { name, language: { name: languageName } },
    );
  }

  editGroup(id: number, name: string, languageName: string): Observable<AdminGroupDTO> {
    return this.http.put<AdminGroupDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.groups.edit(id)),
      { name, language: { name: languageName } },
    );
  }

  deleteGroup(id: number): Observable<void> {
    return this.http
      .delete(
        withOpenApiBase(OPENAPI_PATHS.admin.groups.delete(id)) + `?groupId=${id}`,
      )
      .pipe(map(() => undefined));
  }

  addStudentToGroup(groupId: number, userId: number): Observable<AdminGroupDTO> {
    return this.http.post<AdminGroupDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.groups.addStudent(groupId, userId)),
      {},
    );
  }

  removeStudentFromGroup(groupId: number, userId: number): Observable<AdminGroupDTO> {
    return this.http.delete<AdminGroupDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.groups.removeStudent(groupId, userId)),
    );
  }

  getStudents(): Observable<AdminUserDTO[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.admin.students.list))
      .pipe(
        map((res) => this.normalizeArray<AdminUserDTO>(res)),
        catchError(() => of([])),
      );
  }

  getStudentsByGroupId(groupId: number): Observable<AdminUserDTO[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.admin.students.listByGroup(groupId)))
      .pipe(
        map((res) => this.normalizeArray<AdminUserDTO>(res)),
        catchError(() => of([])),
      );
  }

  createStudent(payload: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    groupIds: number[];
  }): Observable<AdminUserDTO> {
    return this.http.post<AdminUserDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.students.list),
      payload,
    );
  }

  updateStudent(
    id: number,
    payload: { firstName: string; lastName: string },
  ): Observable<AdminUserDTO> {
    return this.http.put<AdminUserDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.students.update(id)),
      payload,
    );
  }

  deleteStudent(id: number): Observable<void> {
    return this.http
      .delete(withOpenApiBase(OPENAPI_PATHS.admin.students.delete(id)))
      .pipe(map(() => undefined));
  }

  getTeachers(): Observable<AdminUserDTO[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.admin.teachers.list))
      .pipe(
        map((res) => this.normalizeArray<AdminUserDTO>(res)),
        catchError(() => of([])),
      );
  }

  getGroupsByTeacher(teacherId: number): Observable<AdminGroupDTO[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.teacher.groupsByTeacher(teacherId)))
      .pipe(
        map((res) => this.normalizeArray<AdminGroupDTO>(res)),
        catchError(() => of([])),
      );
  }

  createTeacher(payload: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }): Observable<AdminUserDTO> {
    return this.http.post<AdminUserDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.teachers.create),
      payload,
    );
  }

  updateTeacher(
    id: number,
    payload: { firstName: string; lastName: string },
  ): Observable<AdminUserDTO> {
    return this.http.put<AdminUserDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.teachers.update(id)),
      payload,
    );
  }

  deleteTeacher(id: number): Observable<void> {
    return this.http
      .delete(withOpenApiBase(OPENAPI_PATHS.admin.teachers.delete(id)))
      .pipe(map(() => undefined));
  }
}
