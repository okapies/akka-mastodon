package com.github.okapies.akka

import java.time.LocalDateTime

import akka.http.scaladsl.model.Uri

package object mastodon {

  // requests
  case class Timeline(path: Uri, local: Boolean)

  object Timeline {
    def home(local: Boolean = false) =
      Timeline("/api/v1/timelines/home", local)
    def public(local: Boolean = false) =
      Timeline("/api/v1/timelines/public", local)
    def tag(hashtag: String, local: Boolean = false) =
      Timeline(s"/api/v1/timelines/home/$hashtag", local)
  }

  // model
  case class Account(
    id: Int,
    username: String,
    acct: String,
    display_name: String,
    locked: Boolean,
    created_at: LocalDateTime,
    followers_count: Int,
    following_count: Int,
    statuses_count: Int,
    note: String,
    url: Uri,
    avatar: Uri,
    avatar_static: Uri,
    header: Uri,
    header_static: Uri
  )

  case class Status(
    id: Int,
    created_at: LocalDateTime,
    in_reply_to_id: Option[Int],
    in_reply_to_account_id: Option[Int],
    sensitive: Boolean,
    spoiler_text: String,
    visibility: String,
    application: Option[Application],
    account: Account,
    media_attachments: Seq[MediaAttachment],
    mentions: Seq[Mention],
    tags: Seq[String],
    uri: Uri,
    content: String,
    raw_content: Option[String],
    url: Uri,
    reblogs_count: Int,
    favourites_count: Int,
    reblog: Option[Status],
    favourited: Option[Boolean],
    reblogged: Option[Boolean]
  )

  case class Application(
    name: String,
    website: Option[Uri]
  )

  case class Mention(
    id: Int,
    acct: String,
    username: String,
    url: Uri
  )

  case class MediaAttachment(
    id: String,
    remote_url: Uri,
    `type`: String,
    url: Uri,
    preview_url: Uri,
    text_url: Uri
  )

}
