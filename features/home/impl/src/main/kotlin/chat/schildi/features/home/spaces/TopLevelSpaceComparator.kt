/*
 * Copyright 2021 The Matrix.org Foundation C.I.C.
 * Copyright 2024 SchildiChat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package chat.schildi.features.home.spaces

// Can't use regular compare by because Null is considered less than any value, and for space order it's the opposite
object SpaceComparator : Comparator<SpaceListDataSource.SpaceHierarchyItem> {
    override fun compare(left: SpaceListDataSource.SpaceHierarchyItem?, right: SpaceListDataSource.SpaceHierarchyItem?): Int {
        val leftOrder = left?.order
        val rightOrder = right?.order
        return if (leftOrder != null && rightOrder != null) {
            leftOrder.compareTo(rightOrder)
        } else {
            if (leftOrder == null) {
                if (rightOrder == null) {
                    // Spec says to fallback to roomId, but we at SchildiChat find lowercase names more suitable
                    //compareValues(left?.info?.roomId?.value, right?.info?.roomId?.value)
                    compareValues(left?.info?.name?.lowercase(), right?.info?.name?.lowercase())
                } else {
                    1
                }
            } else {
                -1
            }
        }
    }
}
