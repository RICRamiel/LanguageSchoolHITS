package com.hits.language_school_back.dto;

import com.hits.language_school_back.model.Group;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterDTO {

    @NotBlank(message = "Имя обязательно")
    @Pattern(
            regexp = "^[А-ЯЁ][а-яё]{1,49}$",
            message = "Имя должно начинаться с заглавной буквы и содержать только буквы русского алфавита (до 50 символов)"
    )
    String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Pattern(
            regexp = "^[А-ЯЁ][а-яё]{1,49}$",
            message = "Фамилия должна начинаться с заглавной буквы и содержать только буквы русского алфавита (до 50 символов)"
    )
    String lastName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, max = 64, message = "Пароль от 8 до 64 символов.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!?\\.])[A-Za-z\\d!?\\.]{8,64}$",
            message = "Только латинские символы, цифры, знаки только !?. Обязательно наличие минимум 1 буквы верхнего и нижнего регистра, цифры и знака."
    )
    String password;

    Group group;
}
