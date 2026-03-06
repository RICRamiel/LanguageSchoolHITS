import {Routes} from '@angular/router';
import {LoginPageComponent} from './pages/login/login-page.component';
import {StudentPageComponent} from './pages/student-page/student-page.component';

import {AdminPageComponent} from './pages/admin-page/admin-page.component';

export const routes: Routes = [
  {path: '', component: LoginPageComponent},
  {path: 'student', component: StudentPageComponent},
  {path: 'admin', component: AdminPageComponent},

import {TeacherPageComponent} from './pages/teacher-page/teacher-page.component';

export const routes: Routes = [
  {path: '', component: LoginPageComponent},
  {path: 'student', component: StudentPageComponent },
  {path: 'teacher', component: TeacherPageComponent },
  {path: '**', redirectTo: ''},
];
