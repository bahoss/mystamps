/*
 * Copyright (C) 2009-2020 Slava Semushin <slava.semushin@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package ru.mystamps.web.feature.series;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import ru.mystamps.web.feature.image.ImageInfoDto;
import ru.mystamps.web.feature.image.ImageService;
import ru.mystamps.web.support.spring.security.HasAuthority;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

// FIXME: move stamps related methods to separate interface (#88)
// The String literal "Series id must be non null" appears N times in this file
// and we think that it's OK.
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals" })
@RequiredArgsConstructor
public class SeriesServiceImpl implements SeriesService {
	
	private final Logger log;
	private final SeriesDao seriesDao;
	private final ImageService imageService;
	private final StampsCatalogService michelCatalogService;
	private final StampsCatalogService scottCatalogService;
	private final StampsCatalogService yvertCatalogService;
	private final StampsCatalogService gibbonsCatalogService;
	private final StampsCatalogService solovyovCatalogService;
	private final StampsCatalogService zagorskiCatalogService;
	
	@Override
	@Transactional
	@PreAuthorize(HasAuthority.CREATE_SERIES)
	public Integer add(AddSeriesDto dto, Integer userId, boolean userCanAddComments) {
		Validate.isTrue(dto != null, "DTO must be non null");
		Validate.isTrue(dto.getQuantity() != null, "Quantity must be non null");
		Validate.isTrue(dto.getPerforated() != null, "Perforated property must be non null");
		Validate.isTrue(dto.getCategory() != null, "Category must be non null");
		Validate.isTrue(dto.getCategory().getId() != null, "Category id must be non null");
		Validate.isTrue(userId != null, "User id must be non null");
		
		AddSeriesDbDto series = new AddSeriesDbDto();
		
		if (dto.getCountry() != null) {
			series.setCountryId(dto.getCountry().getId());
		}
		
		setDateOfReleaseIfProvided(dto, series);
		
		series.setCategoryId(dto.getCategory().getId());
		series.setQuantity(dto.getQuantity());
		series.setPerforated(dto.getPerforated());
		series.setMichelPrice(dto.getMichelPrice());
		series.setScottPrice(dto.getScottPrice());
		series.setYvertPrice(dto.getYvertPrice());
		series.setGibbonsPrice(dto.getGibbonsPrice());
		series.setSolovyovPrice(dto.getSolovyovPrice());
		series.setZagorskiPrice(dto.getZagorskiPrice());

		if (userCanAddComments && dto.getComment() != null) {
			Validate.isTrue(
				!dto.getComment().trim().isEmpty(),
				"Comment must be non empty"
			);
			
			series.setComment(dto.getComment());
		}
		
		Date now = new Date();
		series.setCreatedAt(now);
		series.setUpdatedAt(now);
		
		series.setCreatedBy(userId);
		series.setUpdatedBy(userId);
		
		Integer id = seriesDao.add(series);
		
		createCatalogNumbersAndAddToSeries(id, michelCatalogService, dto.getMichelNumbers());
		createCatalogNumbersAndAddToSeries(id, scottCatalogService, dto.getScottNumbers());
		createCatalogNumbersAndAddToSeries(id, yvertCatalogService, dto.getYvertNumbers());
		createCatalogNumbersAndAddToSeries(id, gibbonsCatalogService, dto.getGibbonsNumbers());
		createCatalogNumbersAndAddToSeries(id, solovyovCatalogService, dto.getSolovyovNumbers());
		createCatalogNumbersAndAddToSeries(id, zagorskiCatalogService, dto.getZagorskiNumbers());
		
		ImageInfoDto imageInfo = imageService.save(dto.getImage());
		Integer imageId = imageInfo.getId();
		
		try {
			imageService.addToSeries(id, imageId);
			
		} catch (RuntimeException ex) { // NOPMD: AvoidCatchingGenericException
			imageService.removeIfPossible(imageInfo);
			throw ex;
		}
		
		log.info("Series #{} has been created ({})", id, series);
		
		return id;
	}

	@Override
	@Transactional
	@PreAuthorize("isAuthenticated()")
	public void addImageToSeries(AddImageDto dto, Integer seriesId, Integer userId) {
		Validate.isTrue(dto != null, "DTO must be non null");
		Validate.isTrue(seriesId != null, "Series id must be non null");
		Validate.isTrue(userId != null, "User id must be non null");
		
		seriesDao.markAsModified(seriesId, new Date(), userId);
		
		ImageInfoDto imageInfo = imageService.save(dto.getImage());
		Integer imageId = imageInfo.getId();
		
		try {
			imageService.addToSeries(seriesId, imageId);
			
		} catch (RuntimeException ex) { // NOPMD: AvoidCatchingGenericException
			imageService.removeIfPossible(imageInfo);
			throw ex;
		}
	}
	
	// @todo #1303 SeriesServiceImpl.replaceImage(): add unit tests
	@Override
	@Transactional
	@PreAuthorize(HasAuthority.REPLACE_IMAGE)
	public void replaceImage(ReplaceImageDto dto, Integer seriesId, Integer userId) {
		Validate.isTrue(dto != null, "DTO must be non null");
		Validate.isTrue(seriesId != null, "Series id must be non null");
		Validate.isTrue(userId != null, "User id must be non null");
		
		seriesDao.markAsModified(seriesId, new Date(), userId);
		
		imageService.replace(dto.getImageId(), dto.getImage());
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countAll() {
		return seriesDao.countAll();
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countAllStamps() {
		return seriesDao.countAllStamps();
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countAddedSince(Date date) {
		Validate.isTrue(date != null, "Date must be non null");
		
		return seriesDao.countAddedSince(date);
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countUpdatedSince(Date date) {
		Validate.isTrue(date != null, "Date must be non null");
		
		return seriesDao.countUpdatedSince(date);
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isSeriesExist(Integer seriesId) {
		Validate.isTrue(seriesId != null, "Series id must be non null");
		
		return seriesDao.countSeriesById(seriesId) > 0;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Integer findQuantityById(Integer seriesId) {
		Validate.isTrue(seriesId != null, "Series id must be non null");
		
		return seriesDao.findQuantityById(seriesId);
	}
	
	@Override
	@Transactional(readOnly = true)
	public SeriesDto findFullInfoById(Integer seriesId, String lang) {
		Validate.isTrue(seriesId != null, "Series id must be non null");
		
		SeriesFullInfoDto seriesBaseInfo = seriesDao.findByIdAsSeriesFullInfo(seriesId, lang);
		if (seriesBaseInfo == null) {
			return null;
		}
		
		List<String> michelNumbers  = michelCatalogService.findBySeriesId(seriesId);
		List<String> scottNumbers   = scottCatalogService.findBySeriesId(seriesId);
		List<String> yvertNumbers   = yvertCatalogService.findBySeriesId(seriesId);
		List<String> gibbonsNumbers = gibbonsCatalogService.findBySeriesId(seriesId);
		List<String> solovyovNumbers = solovyovCatalogService.findBySeriesId(seriesId);
		List<String> zagorskiNumbers = zagorskiCatalogService.findBySeriesId(seriesId);
		
		List<Integer> imageIds = imageService.findBySeriesId(seriesId);
		
		return new SeriesDto(
			seriesBaseInfo,
			michelNumbers,
			scottNumbers,
			yvertNumbers,
			gibbonsNumbers,
			solovyovNumbers,
			zagorskiNumbers,
			imageIds
		);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesInfoDto> findByMichelNumber(String michelNumberCode, String lang) {
		return findByCatalogNumber(michelCatalogService, michelNumberCode, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesInfoDto> findByScottNumber(String scottNumberCode, String lang) {
		return findByCatalogNumber(scottCatalogService, scottNumberCode, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesInfoDto> findByYvertNumber(String yvertNumberCode, String lang) {
		return findByCatalogNumber(yvertCatalogService, yvertNumberCode, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesInfoDto> findByGibbonsNumber(String gibbonsNumberCode, String lang) {
		return findByCatalogNumber(gibbonsCatalogService, gibbonsNumberCode, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesInfoDto> findBySolovyovNumber(String solovyovNumberCode, String lang) {
		return findByCatalogNumber(solovyovCatalogService, solovyovNumberCode, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesInfoDto> findByZagorskiNumber(String zagorskiNumberCode, String lang) {
		return findByCatalogNumber(zagorskiCatalogService, zagorskiNumberCode, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesInfoDto> findByCategorySlug(String slug, String lang) {
		Validate.isTrue(slug != null, "Category slug must be non null");
		
		return seriesDao.findByCategorySlugAsSeriesInfo(slug, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesInfoDto> findByCountrySlug(String slug, String lang) {
		Validate.isTrue(slug != null, "Country slug must be non null");
		
		return seriesDao.findByCountrySlugAsSeriesInfo(slug, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesLinkDto> findRecentlyAdded(int quantity, String lang) {
		Validate.isTrue(quantity > 0, "Quantity of recently added series must be greater than 0");
		
		return seriesDao.findLastAdded(quantity, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SeriesLinkDto> findSimilarSeries(Integer seriesId, String lang) {
		Validate.isTrue(seriesId != null, "Series id must be non null");
		
		return seriesDao.findSimilarSeries(seriesId, lang);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<SitemapInfoDto> findAllForSitemap() {
		return seriesDao.findAllForSitemap();
	}
	
	/**
	 * @author Sergey Chechenev
	 */
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize(HasAuthority.VIEW_SERIES_SALES)
	public List<PurchaseAndSaleDto> findPurchasesAndSales(Integer seriesId) {
		Validate.isTrue(seriesId != null, "Series id must be non null");

		return seriesDao.findPurchasesAndSales(seriesId);
	}
	
	// @todo #1280 SeriesServiceImpl.markAsSimilar(): add unit tests
	@Override
	@Transactional
	@PreAuthorize(HasAuthority.MARK_SIMILAR_SERIES)
	public void markAsSimilar(AddSimilarSeriesForm dto) {
		Validate.isTrue(dto != null, "DTO must be non null");
		
		Integer seriesId = dto.getSeriesId();
		Validate.isTrue(seriesId != null, "Series id must be non null");
		
		Integer similarSeriesId = dto.getSimilarSeriesId();
		Validate.isTrue(similarSeriesId != null, "Similar series id must be non null");
		
		seriesDao.markAsSimilar(seriesId, similarSeriesId);
		
		log.info("Series #{} has been marked as similar to #{}", seriesId, similarSeriesId);
	}
	
	private List<SeriesInfoDto> findByCatalogNumber(
		StampsCatalogService catalogService,
		String number, String lang) {
		
		List<Integer> seriesIds = catalogService.findSeriesIdsByNumber(number);
		if (seriesIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		return seriesDao.findByIdsAsSeriesInfo(seriesIds, lang);
	}
	
	private static void setDateOfReleaseIfProvided(AddSeriesDto dto, AddSeriesDbDto series) {
		if (dto.getYear() == null) {
			return;
		}
		
		series.setReleaseYear(dto.getYear());
		
		if (dto.getMonth() == null) {
			return;
		}
		
		series.setReleaseMonth(dto.getMonth());
		
		// even if day is null it won't change anything
		series.setReleaseDay(dto.getDay());
	}
	
	private static void createCatalogNumbersAndAddToSeries(
		Integer seriesId,
		StampsCatalogService catalogService,
		String numbers) {
		
		Set<String> parsedNumbers = CatalogUtils.parseCatalogNumbers(numbers);
		if (!parsedNumbers.isEmpty()) {
			catalogService.add(parsedNumbers);
			catalogService.addToSeries(seriesId, parsedNumbers);
		}
	}
	
}
