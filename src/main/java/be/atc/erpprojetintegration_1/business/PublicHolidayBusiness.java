package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.PublicHoliday;
import be.atc.erpprojetintegration_1.interfaces.IPublicHolidayService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PublicHolidayBusiness {

    @Inject
    private IPublicHolidayService publicHolidayService;

    public Result<List<PublicHoliday>> getAll() {
        return publicHolidayService.getAll();
    }

    public Result<PublicHoliday> save(PublicHoliday publicHoliday) {
        Map<String, String> errors = new HashMap<>();
        if (publicHoliday == null) {
            errors.put("holiday", "publicHolidays.error.form.invalid");
        } else {
            FormValidator.required(publicHoliday.getName(), "name", "publicHolidays.error.name.required", errors);
            FormValidator.lengthBetween(publicHoliday.getName(), "name", "publicHolidays.error.name.length", 1, 150, errors);
            if (publicHoliday.getHolidayDate() == null) {
                errors.put("holidayDate", "publicHolidays.error.date.required");
            }
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        publicHoliday.setName(publicHoliday.getName().trim());
        publicHoliday.setDescription(publicHoliday.getDescription() == null
                ? null : publicHoliday.getDescription().trim());
        publicHoliday.setIsActive(true);
        return publicHolidayService.save(publicHoliday);
    }

    public Result<Void> setActive(Integer id, boolean active) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "publicHolidays.error.id.required");
            return Result.fail(errors);
        }
        return publicHolidayService.setActive(id, active);
    }

    public Result<Void> generateBelgianHolidays(Integer year) {
        if (year == null || year < 1900 || year > 2200) {
            Map<String, String> errors = new HashMap<>();
            errors.put("year", "publicHolidays.error.year.invalid");
            return Result.fail(errors);
        }

        LocalDate easterSunday = calculateEasterSunday(year);
        List<PublicHoliday> publicHolidays = new ArrayList<>();
        publicHolidays.add(create("Jour de l'An", LocalDate.of(year, 1, 1)));
        publicHolidays.add(create("Lundi de Paques", easterSunday.plusDays(1)));
        publicHolidays.add(create("Fete du Travail", LocalDate.of(year, 5, 1)));
        publicHolidays.add(create("Ascension", easterSunday.plusDays(39)));
        publicHolidays.add(create("Lundi de Pentecote", easterSunday.plusDays(50)));
        publicHolidays.add(create("Fete nationale", LocalDate.of(year, 7, 21)));
        publicHolidays.add(create("Assomption", LocalDate.of(year, 8, 15)));
        publicHolidays.add(create("Toussaint", LocalDate.of(year, 11, 1)));
        publicHolidays.add(create("Armistice", LocalDate.of(year, 11, 11)));
        publicHolidays.add(create("Noel", LocalDate.of(year, 12, 25)));
        return publicHolidayService.saveAll(publicHolidays);
    }

    private PublicHoliday create(String name, LocalDate date) {
        PublicHoliday publicHoliday = new PublicHoliday();
        publicHoliday.setName(name);
        publicHoliday.setHolidayDate(date);
        publicHoliday.setDescription("Jour ferie legal belge.");
        publicHoliday.setIsActive(true);
        return publicHoliday;
    }

    private LocalDate calculateEasterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
