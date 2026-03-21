create DATABASE lang_school;

GRANT ALL PRIVILEGES ON DATABASE lang_school TO c417110f943e48c5a62d45633bdf9ea2;

INSERT INTO public.users (email,first_name,last_name,"password","role") VALUES
                                                                            ('string@string.string','String','String','$2a$12$S5YXTaTHqHoBwblocjwM0ODNQR9uU5Jv7fOAVRzUdoBtN1bj0Dp2e','ADMIN'),
                                                                            ('string_teacher@string.string','StringTEACHER','StringTEACHER','$2a$12$S5YXTaTHqHoBwblocjwM0ODNQR9uU5Jv7fOAVRzUdoBtN1bj0Dp2e','TEACHER'),
                                                                            ('string_student@string.string','StringSTUDENT','StringSTUDENT','$2a$12$S5YXTaTHqHoBwblocjwM0ODNQR9uU5Jv7fOAVRzUdoBtN1bj0Dp2e','STUDENT');
