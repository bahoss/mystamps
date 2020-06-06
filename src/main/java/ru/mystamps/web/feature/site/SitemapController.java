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
package ru.mystamps.web.feature.site;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.mystamps.web.feature.series.SeriesService;
import ru.mystamps.web.feature.series.SeriesUrl;
import ru.mystamps.web.feature.series.SitemapInfoDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class SitemapController {
	private static final Logger LOG = LoggerFactory.getLogger(SitemapController.class);
	
	// FIXME: convert dates to UTC and omit server-specific timezone part
	// According to http://www.w3.org/TR/NOTE-datetime
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mmXXX";
	
	private final SeriesService seriesService;
	
	@GetMapping(SiteUrl.SITEMAP_XML)
	public void generateSitemapXml(HttpServletResponse response) {
		response.setContentType(MediaType.APPLICATION_XML_VALUE);
		response.setCharacterEncoding("UTF-8");
		
		DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
		dateFormatter.setLenient(false);
		
		try {
			PrintWriter writer = response.getWriter();
			
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
			
			writer.print("<url><loc>");
			writer.print(SiteUrl.PUBLIC_URL);
			writer.print(SiteUrl.INDEX_PAGE);
			writer.println("</loc></url>");
			
			for (SitemapInfoDto item : seriesService.findAllForSitemap()) {
				writer.print("<url><loc>");
				writer.print(createLocEntry(item));
				writer.print("</loc><lastmod>");
				writer.print(createLastModEntry(dateFormatter, item));
				writer.println("</lastmod></url>");
			}
			
			writer.println("</urlset>");
		} catch (IOException ex) {
			LOG.error("Can't return sitemap.xml: {}", ex.getMessage());
		}
	}
	
	private static String createLocEntry(SitemapInfoDto item) {
		return SiteUrl.PUBLIC_URL
			+ SeriesUrl.INFO_SERIES_PAGE.replace("{id}", String.valueOf(item.getId()));
	}
	
	private static String createLastModEntry(DateFormat dateFormatter, SitemapInfoDto item) {
		return dateFormatter.format(item.getUpdatedAt());
	}
	
}

