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

import ru.mystamps.web.common.Currency;
import ru.mystamps.web.common.JdbcUtils;
import ru.mystamps.web.common.LinkEntityDto;
import ru.mystamps.web.feature.series.sale.SeriesCondition;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static ru.mystamps.web.common.RowMappers.createLinkEntityDto;

// complains on "release_year", "quantity" and "perforated"
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class RowMappers {
	
	private RowMappers() {
	}
	
	/* default */ static SitemapInfoDto forSitemapInfoDto(ResultSet rs, int unused)
		throws SQLException {
		
		return new SitemapInfoDto(
			rs.getInt("id"),
			rs.getTimestamp("updated_at")
		);
	}

	/* default */ static SeriesLinkDto forSeriesLinkDto(ResultSet rs, int unused)
		throws SQLException {
		
		Integer id         = rs.getInt("id");
		Integer year       = JdbcUtils.getInteger(rs, "release_year");
		Integer quantity   = rs.getInt("quantity");
		Boolean perforated = rs.getBoolean("perforated");
		String country     = rs.getString("country_name");
		
		return new SeriesLinkDto(id, year, quantity, perforated, country);
	}

	/* default */ static SeriesInfoDto forSeriesInfoDto(ResultSet rs, int unused)
		throws SQLException {
		
		Integer seriesId     = rs.getInt("id");
		Integer releaseYear  = JdbcUtils.getInteger(rs, "release_year");
		Integer quantity     = rs.getInt("quantity");
		Boolean perforated   = rs.getBoolean("perforated");
		
		LinkEntityDto category =
			createLinkEntityDto(rs, "category_id", "category_slug", "category_name");
		LinkEntityDto country =
			createLinkEntityDto(rs, "country_id", "country_slug", "country_name");
		
		return new SeriesInfoDto(
			seriesId,
			category,
			country,
			releaseYear,
			quantity,
			perforated
		);
	}
	
	/* default */ static SeriesInGalleryDto forSeriesInGalleryDto(ResultSet rs, int unused)
		throws SQLException {
		
		Integer seriesId    = rs.getInt("id");
		Integer releaseYear = JdbcUtils.getInteger(rs, "release_year");
		Integer quantity    = rs.getInt("quantity");
		Boolean perforated  = rs.getBoolean("perforated");
		Integer previewId   = JdbcUtils.getInteger(rs, "preview_id");
		String category     = rs.getString("category");
		
		return new SeriesInGalleryDto(
			seriesId,
			releaseYear,
			quantity,
			perforated,
			previewId,
			category
		);
	}
	
	/**
	 * @author Sergey Chechenev
	 */
	/* default */ static PurchaseAndSaleDto forPurchaseAndSaleDto(ResultSet rs, int unused)
		throws SQLException {
		
		Date date               = rs.getDate("date");
		String sellerName       = rs.getString("seller_name");
		String sellerUrl        = rs.getString("seller_url");
		String buyerName        = rs.getString("buyer_name");
		String buyerUrl         = rs.getString("buyer_url");
		String transactionUrl   = rs.getString("transaction_url");
		BigDecimal firstPrice   = rs.getBigDecimal("first_price");
		Currency firstCurrency  = JdbcUtils.getCurrency(rs, "first_currency");
		BigDecimal secondPrice  = rs.getBigDecimal("second_price");
		Currency secondCurrency = JdbcUtils.getCurrency(rs, "second_currency");
		
		// LATER: consider extracting this into a helper method
		String conditionField = rs.getString("cond");
		SeriesCondition condition = rs.wasNull() ? null : SeriesCondition.valueOf(conditionField);
		
		return new PurchaseAndSaleDto(
			date,
			sellerName,
			sellerUrl,
			buyerName,
			buyerUrl,
			transactionUrl,
			firstPrice,
			firstCurrency,
			secondPrice,
			secondCurrency,
			condition
		);
	}

	/* default */ static SeriesFullInfoDto forSeriesFullInfoDto(ResultSet rs, int unused)
		throws SQLException {
		
		Integer seriesId     = rs.getInt("id");
		Integer releaseDay   = JdbcUtils.getInteger(rs, "release_day");
		Integer releaseMonth = JdbcUtils.getInteger(rs, "release_month");
		Integer releaseYear  = JdbcUtils.getInteger(rs, "release_year");
		Integer quantity     = rs.getInt("quantity");
		Boolean perforated   = rs.getBoolean("perforated");
		String comment       = rs.getString("comment");
		Integer createdBy    = rs.getInt("created_by");
		
		BigDecimal michelPrice   = rs.getBigDecimal("michel_price");
		BigDecimal scottPrice    = rs.getBigDecimal("scott_price");
		BigDecimal yvertPrice    = rs.getBigDecimal("yvert_price");
		BigDecimal gibbonsPrice  = rs.getBigDecimal("gibbons_price");
		BigDecimal solovyovPrice = rs.getBigDecimal("solovyov_price");
		BigDecimal zagorskiPrice = rs.getBigDecimal("zagorski_price");
		
		LinkEntityDto category =
			createLinkEntityDto(rs, "category_id", "category_slug", "category_name");
		
		LinkEntityDto country =
			createLinkEntityDto(rs, "country_id", "country_slug", "country_name");
		
		return new SeriesFullInfoDto(
			seriesId,
			category,
			country,
			releaseDay,
			releaseMonth,
			releaseYear,
			quantity,
			perforated,
			comment,
			createdBy,
			michelPrice,
			scottPrice,
			yvertPrice,
			gibbonsPrice,
			solovyovPrice,
			zagorskiPrice
		);
	}
	
}
