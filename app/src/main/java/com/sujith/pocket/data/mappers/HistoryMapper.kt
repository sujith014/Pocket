package com.sujith.pocket.data.mappers

import com.sujith.pocket.data.model.HistoryEntity
import com.sujith.pocket.domain.model.HistoryDto

fun HistoryEntity.toHistoryDto(): HistoryDto = HistoryDto(
    title = this.title,
    url = this.url,
    date = this.date
)

fun HistoryDto.toHistoryEntity(): HistoryEntity = HistoryEntity(
    title = this.title,
    url = this.url,
    date = this.date
)