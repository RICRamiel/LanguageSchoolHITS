import {Routes} from '@angular/router';
import {LoginPageComponent} from './pages/login/login-page.component';
import {StudentPageComponent} from './pages/student-page/student-page.component';

export const routes: Routes = [
  {path: '', component: LoginPageComponent},
  {path: 'student', component: StudentPageComponent },
  {path: '**', redirectTo: ''},
];
``
