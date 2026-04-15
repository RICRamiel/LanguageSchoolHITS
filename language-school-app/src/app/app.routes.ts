import {Routes} from '@angular/router';
import {LoginPageComponent} from './pages/login/login-page.component';
import {StudentPageComponent} from './pages/student-page/student-page.component';
import {TeacherPageComponent} from './pages/teacher-page/teacher-page.component';
import {AdminPageComponent} from './pages/admin-page/admin-page.component';
import {authGuard} from './core/auth/auth.guard';

export const routes: Routes = [
  {path: '', component: LoginPageComponent},
  {path: 'student', component: StudentPageComponent, canActivate: [authGuard]},
  {path: 'teacher', component: TeacherPageComponent, canActivate: [authGuard]},
  {path: 'admin', component: AdminPageComponent, canActivate: [authGuard]},
  {path: '**', redirectTo: ''},
];
