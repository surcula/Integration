package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.PublicHoliday;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Defines public holiday management operations.
 */
public interface IPublicHolidayService {

    /**
     * Retrieves all public holidays ordered by date.
     *
     * @return public holiday list result
     */
    Result<List<PublicHoliday>> getAll();

    /**
     * Creates or updates a public holiday entry.
     *
     * @param publicHoliday public holiday to save
     * @return saved public holiday result
     */
    Result<PublicHoliday> save(PublicHoliday publicHoliday);

    /**
     * Creates or updates a batch of public holidays.
     * Existing entries for the same date are updated rather than duplicated.
     *
     * @param publicHolidays list of public holidays to save
     * @return operation result
     */
    Result<Void> saveAll(List<PublicHoliday> publicHolidays);

    /**
     * Updates the active status of a public holiday.
     *
     * @param id     public holiday id
     * @param active active status to apply
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);
}
