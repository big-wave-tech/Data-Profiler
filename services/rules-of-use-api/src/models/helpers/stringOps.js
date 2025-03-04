/*
  Copyright 2021 Merck & Co., Inc. Kenilworth, NJ, USA.
 
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements. See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership. The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License. You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
 
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
import got from 'got'
import { uniq } from 'lodash'

export const normalize = str => {
  if (!str) return ''
  if (typeof str !== 'string') str = str.toString()
  return str.trim()
}

const apiHost = process.env.DP_API_SERVICE_PORT ?
  `http://api:${process.env.DP_API_SERVICE_PORT}` : 'http://frontend-docker:7001'

export const escapeAccumulo = inputArray =>
  new Promise(async resolve => {
    const res = await Promise.all(
      inputArray.map(attribute =>
        got(`${apiHost}/accumulo_syntax/attribute/escape`, {
          json: false,
          method: 'POST',
          body: attribute,
          headers: { 'Content-Type': 'text/plain' }
        })
          .then(res => normalize(res.body))
          .catch(() => null)
      )
    )
    resolve(uniq(res))
  })
