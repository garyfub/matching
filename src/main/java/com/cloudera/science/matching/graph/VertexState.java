/**
 * Copyright (c) 2012, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package com.cloudera.science.matching.graph;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

import com.google.common.collect.Maps;

/**
 * Maintains the internal state of a vertex in the bipartite graph within a
 * Giraph job. 
 */
public class VertexState implements Writable {

  private boolean bidder;
  private Text matchId = new Text();
  private BigDecimal price = BigDecimal.ZERO;
  private Map<Text, BigDecimal> priceIndex = Maps.newHashMap();
  
  public VertexState() { }
  
  public VertexState(boolean bidder) {
    this.bidder = bidder;
  }
  
  public VertexState(boolean bidder, Text matchId, BigDecimal price, Map<Text, BigDecimal> priceIndex) {
    this.bidder = bidder;
    this.matchId = matchId;
    this.price = price;
    this.priceIndex = priceIndex;
  }
  
  public boolean isBidder() {
    return bidder;
  }
  
  public Text getMatchId() {
    return matchId;
  }
  
  public void setMatchId(Text ownerId) {
    this.matchId = ownerId;
  }
  
  public void clearMatchId() {
    this.matchId = new Text();
  }
  
  public BigDecimal getPrice() {
    return price;
  }
  
  public void setPrice(BigDecimal price) {
    this.price = price;
  }
  
  public Map<Text, BigDecimal> getPriceIndex() {
    return priceIndex;
  }
  
  @Override
  public void readFields(DataInput in) throws IOException {
    bidder = in.readBoolean();
    if (!bidder) {
      price = new BigDecimal(in.readUTF());
    }
    matchId.readFields(in);
    this.priceIndex = Maps.newHashMap();
    int sz = WritableUtils.readVInt(in);
    for (int i = 0; i < sz; i++) {
      Text vertexId = new Text();
      vertexId.readFields(in);
      String price = in.readUTF();
      priceIndex.put(vertexId, new BigDecimal(price));
    }
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeBoolean(bidder);
    if (!bidder) {
      out.writeUTF(price.toString());
    }
    matchId.write(out);
    WritableUtils.writeVInt(out, priceIndex.size());
    for (Map.Entry<Text, BigDecimal> e : priceIndex.entrySet()) {
      e.getKey().write(out);
      out.writeUTF(e.getValue().toString());
    }
  }
}
