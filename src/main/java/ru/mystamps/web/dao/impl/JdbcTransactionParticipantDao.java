/*
 * Copyright (C) 2009-2017 Slava Semushin <slava.semushin@gmail.com>
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
package ru.mystamps.web.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import lombok.RequiredArgsConstructor;

import ru.mystamps.web.dao.TransactionParticipantDao;
import ru.mystamps.web.dao.dto.AddParticipantDbDto;
import ru.mystamps.web.dao.dto.EntityWithIdDto;
import ru.mystamps.web.dao.dto.TransactionParticipantDto;

@RequiredArgsConstructor
public class JdbcTransactionParticipantDao implements TransactionParticipantDao {
	
	private final NamedParameterJdbcTemplate jdbcTemplate;
	
	@Value("${transaction_participant.create}")
	private String addParticipantSql;
	
	@Value("${transaction_participant.find_buyers_with_parent_names}")
	private String findBuyersWithParentNamesSql;
	
	@Value("${transaction_participant.find_sellers_with_parent_names}")
	private String findSellersWithParentNamesSql;
	
	@Value("${transaction_participant_group.find_all}")
	private String findAllGroupsSql;
	
	@Override
	public void add(AddParticipantDbDto participant) {
		Map<String, Object> params = new HashMap<>();
		params.put("name", participant.getName());
		params.put("url", participant.getUrl());
		params.put("group_id", participant.getGroupId());
		params.put("buyer", participant.getBuyer());
		params.put("seller", participant.getSeller());
		
		int affected = jdbcTemplate.update(addParticipantSql, params);
		
		Validate.validState(
			affected == 1,
			"Unexpected number of affected rows after creation of participant: %d",
			affected
		);
	}
	
	@Override
	public List<TransactionParticipantDto> findBuyersWithParents() {
		return jdbcTemplate.query(
			findBuyersWithParentNamesSql,
			RowMappers::forTransactionParticipantDto
		);
	}
	
	@Override
	public List<TransactionParticipantDto> findSellersWithParents() {
		return jdbcTemplate.query(
			findSellersWithParentNamesSql,
			RowMappers::forTransactionParticipantDto
		);
	}
	
	@Override
	public List<EntityWithIdDto> findAllGroups() {
		return jdbcTemplate.query(findAllGroupsSql, RowMappers::forEntityWithIdDto);
	}
	
}
