import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { OPENAPI_PATHS, withOpenApiBase } from '../api/openapi.config';

export type AdminLanguageDTO = { name?: string; id?: string };
export type AdminGroupDTO = {
  id?: string;
  name?: string;
  description?: string;
  language?: { name?: string };
};
export type AdminUserDTO = {
  id?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  groups?: Array<{ id?: string; name?: string }>;
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

  editLanguage(id: string, name: string): Observable<AdminLanguageDTO> {
    return this.http.put<AdminLanguageDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.languages.edit(id)),
      { name },
    );
  }

  deleteLanguage(id: string): Observable<void> {
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

  createGroup(
    name: string,
    teacherId: string,
    languageId: string,
    description: string,
    satisfactorilyMarkThreshold: number,
    goodMarkThreshold: number,
    excellentMarkThreshold: number,
  ): Observable<AdminGroupDTO> {
    return this.http.post<AdminGroupDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.groups.create),
      { name, teacherId, languageId, description, satisfactorilyMarkThreshold, goodMarkThreshold, excellentMarkThreshold },
    );
  }

  editGroup(id: string, name: string): Observable<AdminGroupDTO> {
    return this.http.put<AdminGroupDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.groups.edit(id)),
      { name },
    );
  }

  deleteGroup(id: string): Observable<void> {
    return this.http
      .delete(withOpenApiBase(OPENAPI_PATHS.admin.groups.delete) + `?courseId=${encodeURIComponent(id)}`)
      .pipe(map(() => undefined));
  }

  addStudentToGroup(courseId: string, studentId: string): Observable<void> {
    return this.http
      .post<unknown>(
        withOpenApiBase(OPENAPI_PATHS.admin.groups.addStudents),
        { courseId, studentIds: [studentId] },
      )
      .pipe(map(() => undefined));
  }

  removeStudentFromGroup(_courseId: string, _studentId: string): Observable<void> {
    // Backend has no remove-student-from-course endpoint
    return of(undefined);
  }

  getStudents(): Observable<AdminUserDTO[]> {
    return this.http
      .get<unknown>(withOpenApiBase(OPENAPI_PATHS.admin.students.list))
      .pipe(
        map((res) => this.normalizeArray<AdminUserDTO>(res)),
        catchError(() => of([])),
      );
  }

  getStudentsByGroupId(groupId: string): Observable<AdminUserDTO[]> {
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
    groupIds: string[];
  }): Observable<AdminUserDTO> {
    return this.http.post<AdminUserDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.students.list),
      payload,
    );
  }

  updateStudent(
    id: string,
    payload: { firstName: string; lastName: string },
  ): Observable<AdminUserDTO> {
    return this.http.put<AdminUserDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.students.update(id)),
      payload,
    );
  }

  deleteStudent(id: string): Observable<void> {
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

  getGroupsByTeacher(_teacherId: string): Observable<AdminGroupDTO[]> {
    // Backend has no "groups by teacher" endpoint — return all courses
    return this.getGroups();
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
    id: string,
    payload: { firstName: string; lastName: string },
  ): Observable<AdminUserDTO> {
    return this.http.put<AdminUserDTO>(
      withOpenApiBase(OPENAPI_PATHS.admin.teachers.update(id)),
      payload,
    );
  }

  deleteTeacher(id: string): Observable<void> {
    return this.http
      .delete(withOpenApiBase(OPENAPI_PATHS.admin.teachers.delete(id)))
      .pipe(map(() => undefined));
  }
}
