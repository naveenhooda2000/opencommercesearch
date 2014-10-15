package org.opencommercesearch.api.models

/*
* Licensed to OpenCommerceSearch under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. OpenCommerceSearch licenses this
* file to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.util

import org.opencommercesearch.api.util.JsUtils.PathAdditions
import org.opencommercesearch.search.suggester.IndexableElement

import org.apache.commons.lang.StringUtils
import org.apache.solr.common.SolrInputDocument

import org.jongo.marshall.jackson.oid.Id

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A category model.
 *
 * @param id is the category id
 * @param name is the category name
 * @param isRuleBased indicates if this a rule based category or not
 * @param sites the sites the categories belongs to
 * @param parentCategories the parent categories. Empty for root categories
 *
 * @author rmerizalde
 */
case class Category (
  @Id var id: Option[String] = None,
  @JsonProperty("name") var name: Option[String] = None,
  @JsonProperty("alias") var alias: Option[String] = None,
  @JsonProperty("seoUrlToken") var seoUrlToken: Option[String] = None,
  @JsonProperty("onSaleUrl") var onSaleUrl: Option[String] = None,
  @JsonProperty("isRuleBased") var isRuleBased: Option[Boolean] = None,
  @JsonProperty("ruleFilters") var ruleFilters: Option[Seq[String]] = None,
  @JsonProperty("sites") var sites: Option[Seq[String]] = None,
  @JsonProperty("hierarchyTokens") var hierarchyTokens: Option[Seq[String]] = None,
  @JsonProperty("parentCategories") var parentCategories: Option[Seq[Category]] = None,
  @JsonProperty("childCategories") var childCategories: Option[Seq[Category]] = None) extends IndexableElement {

  import Category._

  def getId : String = this.id.get

  def getName : String = this.name.getOrElse(StringUtils.EMPTY)

  def getUrl : String = this.seoUrlToken.getOrElse(StringUtils.EMPTY)

  override def source = "category"

  override def toJson : JsValue = Json.toJson(this)

  def getNgramText : String = this.getName

  def getType : String = "category"

  def getSites : Seq[String] = this.sites.getOrElse(Seq.empty[String])

}

object Category {
  val defaultFields = Seq("id", "name", "seoUrlToken")

  def getInstance(id: Option[String]) = new Category(id)

  /**
   * Creates a copy of the given category that contains only the fields specified.
   * <p/>
   * If fields is empty, then all existing field of the category are copied over.
   * @param category Category to copy fields from.
   * @param fields is the list of fields to copy from the given category. If empty, all fields are copied.
   * @param excludedFields is the list of fields to exclude from the new category
   * @return A copy of the given category with only the fields specified set.
   */
  def copyWithFields(category: Category, fields: Seq[String], excludedFields: Seq[String] = Seq()) : Category = {
    val copy = getInstance(category.id)

    var fieldsToCopy = defaultFields
    if(!fields.isEmpty) {
      fieldsToCopy = fields
    }

    val hasStar = fieldsToCopy.contains("*")
    def isCopyField(name: String) = !excludedFields.contains(name) && (hasStar || fieldsToCopy.contains(name))

    if(isCopyField("name")) {
      copy.name = category.name
    }

    if(isCopyField("alias")) {
      copy.alias = category.alias
    }

    if(isCopyField("seoUrlToken")) {
      copy.seoUrlToken = category.seoUrlToken
    }

    if(isCopyField("onSaleUrl")) {
      copy.onSaleUrl = category.onSaleUrl
    }

    if(isCopyField("isRuleBased")) {
      copy.isRuleBased = category.isRuleBased
    }

    if(isCopyField("ruleFilters")) {
      copy.ruleFilters = category.ruleFilters
    }

    if(isCopyField("sites")) {
      copy.sites = category.sites
    }

    if(isCopyField("parentCategories")) {
      copy.parentCategories = category.parentCategories
    }

    if(isCopyField("childCategories")) {
      copy.childCategories = category.childCategories
    }
    copy
  }

  /**
   * Helper method that recursively trims the given category node.
   * <ul>
   * <li>Removes all categories not in the given categories set. This is used to exclude categories that don't have products in stock.
   * <li>Removes all child categories that are not in the given depth level.
   * <li>Returns only maxChildren category nodes on leaf nodes.
   * <ul/>
   * @param category The category to trim.
   * @param categories Set of categories to return. If a child category is not on this set, is removed. If this set is empty, no filtering is done.
   * @param depth Depth level to trim until to. If zero, returns the current category. If less than zero, returns null.
   * @param maxChildren Max children to return on category leaves.
   * @return The given category less all the category nodes that were trimmed following the above criteria.
   */
  def prune(category: Category, categories: Set[String], depth: Int, maxChildren: Int, fields: Seq[String]) : Category = {
    if(depth >= 0) {
      val categoryCopy = copyCategory(category, fields)
      var childCats = categoryCopy.childCategories.getOrElse(Seq.empty)

      childCats = childCats map { childCat =>
        if(categories.isEmpty || categories.contains(childCat.getId)) {
          prune(childCat, categories, depth - 1, maxChildren, fields)
        }
        else {
          null
        }
      } filter { _ != null}

      if(depth == 1 && maxChildren >= 0) {
        childCats = childCats.take(maxChildren)
      }

      categoryCopy.childCategories = Option(childCats)
      categoryCopy
    }
    else {
      null
    }
  }

  /**
   * Returns a new category with only the fields specified. Does the same recursively for parent and child categories.
   * <p/>
   * If star ("*") is on the field list, then an exact copy of the given category is returned. If fields is empty, then the copy category only contains
   * the default fields.
   * <p/>
   * This method ensures you have a new copy of the category data to work on.
   * @param fields is the list of fields to include in the new category
   * @param excludedFields is the list of fields to exclude from the new category
   */
  def copyCategory(category: Category, fields: Seq[String], excludedFields: Seq[String] = Seq()) : Category = {
    var fieldsToCopy:Seq[String] = fields match {
      case Nil => defaultFields
      case _ => fields
    }
    copyWithFields(category, fieldsToCopy, excludedFields)
  }

  implicit val readsCategory : Reads[Category] = (
    (__ \ "id").readNullable[String] ~
    (__ \ "name").readNullable[String] ~
    (__ \ "alias").readNullable[String] ~
    (__ \ "seoUrlToken").readNullable[String] ~
    (__ \ "onSaleUrl").readNullable[String] ~
    (__ \ "isRuleBased").readNullable[Boolean] ~
    (__ \ "ruleFilters").readNullable[Seq[String]] ~
    (__ \ "sites").readNullable[Seq[String]] ~
    (__ \ "hierarchyTokens").readNullable[Seq[String]] ~
    (__ \ "parentCategories").lazyReadNullable(Reads.list[Category](readsCategory)) ~
    (__ \ "childCategories").lazyReadNullable(Reads.list[Category](readsCategory))
  ) (Category.apply _)

  implicit val writesCategory : Writes[Category] = (
    (__ \ "id").writeNullable[String] ~
    (__ \ "name").writeNullable[String] ~
    (__ \ "alias").writeNullable[String] ~
    (__ \ "seoUrlToken").writeNullable[String] ~
    (__ \ "onSaleUrl").writeNullable[String] ~
    (__ \ "isRuleBased").writeNullable[Boolean] ~
    (__ \ "ruleFilters").writeNullable[Seq[String]] ~
    (__ \ "sites").writeNullable[Seq[String]] ~
    (__ \ "hierarchyTokens").writeNullable[Seq[String]] ~
    (__ \ "parentCategories").lazyWriteNullable(Writes.traversableWrites[Category](writesCategory)) ~
    //Prevent empty child lists to be written
    (__ \ "childCategories").lazyWriteNullableIterable[Seq[Category]](Writes.traversableWrites[Category](writesCategory))
  ) (unlift(Category.unapply))
}

case class CategoryList(categories: Seq[Category], feedTimestamp: Long) {
  def toDocuments: util.List[SolrInputDocument] = {
    val documents = new util.ArrayList[SolrInputDocument](categories.size)
    var expectedDocCount = 0
    var currentDocCount = 0

    for (category: Category <- categories) {
      expectedDocCount += 1
      for (id <- category.id; name <- category.name; isRuleBased <- category.isRuleBased) {
        val doc = new SolrInputDocument()

        doc.setField("id", id)
        doc.setField("name", name)
        doc.setField("isRuleBased", isRuleBased)
        doc.setField("feedTimestamp", feedTimestamp)

        var hasSites = false
        for (sites <- category.sites) {
          hasSites = sites.size > 0
          for (site <- sites) {
            doc.addField("siteId", site)

            //TODO Remove this
            doc.addField("catalogs", site)
          }
        }

        var hasParents = false
        for (parentCategories <- category.parentCategories) {
          hasParents = parentCategories.size > 0
          for (parentCategory <- parentCategories) {
            for (id <- parentCategory.id) {
              doc.addField("parentCategories", id)
            }
          }
        }

        var hasChildren = false
        for (childCategories <- category.childCategories) {
          hasChildren = childCategories.size > 0
          for (childCategory <- childCategories) {
            for (id <- childCategory.id) {
              doc.addField("childCategories", id)
            }
          }
        }

        // this is just informational info
        if (hasSites) {
          if (!hasParents && hasChildren) {
            doc.setField("isRoot", true)
          } else if (hasParents && hasChildren) {
            doc.setField("isNode", true)
          } else if (hasParents && !hasChildren) {
            doc.setField("isLeaf", true)
          } else {
            // shouldn't happen
            doc.setField("isOrphan", true)
          }
        } else {
          doc.setField("isOrphan", true)
        }

        documents.add(doc)
        currentDocCount += 1
      }

      if (expectedDocCount != currentDocCount) {
        throw new IllegalArgumentException("Missing required fields for category " + category.id.get)
      }
    }

    documents
  }
}

object CategoryList {
  implicit val readsCategoryList = Json.reads[CategoryList]
  implicit val writesCategoryList = Json.writes[CategoryList]
}