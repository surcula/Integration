package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.PublicHoliday;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IPublicHolidayService {
    Result<List<PublicHoliday>> getAll();
    Result<PublicHoliday> save(PublicHoliday publicHoliday);
    Result<Void> saveAll(List<PublicHoliday> publicHolidays);
    Result<Void> setActive(Integer id, boolean active);
}
