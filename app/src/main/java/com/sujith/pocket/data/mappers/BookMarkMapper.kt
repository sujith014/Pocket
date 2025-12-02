package com.sujith.pocket.data.mappers

import com.sujith.pocket.data.model.BookMarkEntity
import com.sujith.pocket.domain.model.BookMarkDto

fun BookMarkDto.toBookMarkEntity(): BookMarkEntity{
    return BookMarkEntity(
        id = this.id,
        url = this.url,
        title = this.title,
        date = this.date
    )
}

fun BookMarkEntity.toBookMarkDto(): BookMarkDto{
    return BookMarkDto(
        id = this.id,
        url = this.url,
        title = this.title,
        date = this.date
    )
}