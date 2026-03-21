package com.hits.language_school_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterStudentDTO {

    @NotBlank(message = "Имя обязательно")
//    @Pattern(
//            regexp = "^[А-ЯЁ][а-яё]{1,49}$",
//            message = "Имя должно начинаться с заглавной буквы и содержать только буквы русского алфавита (до 50 символов)"
//    )
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
//    @Pattern(
//            regexp = "^[А-ЯЁ][а-яё]{1,49}$",
//            message = "Фамилия должна начинаться с заглавной буквы и содержать только буквы русского алфавита (до 50 символов)"
//    )
    private String lastName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 64, message = "Пароль от 8 до 64 символов.")
//    @Pattern(
//            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!?\\.])[A-Za-z\\d!?\\.]{8,64}$",
//            message = "Только латинские символы, цифры, знаки только !?. Обязательно наличие минимум 1 буквы верхнего и нижнего регистра, цифры и знака."
//    )
    private String password;

    private String grade;
}
