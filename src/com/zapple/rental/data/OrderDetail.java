/*
 * Copyright (C) 2012 Li Cong, forlong401@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zapple.rental.data;

public class OrderDetail {
    /**
     * The unique ID for a row.
     * <P>Type: INTEGER (long)</P>
     */
	public String mOrderStatus;
	public String mVehicleModel;
	public String mVehicleBrand;
	public String mTakeVehicleStoreName;    
    public String mTakeVehicleDate;
    public String mReturnVehicleStoreName;
    public String mReturnVehicleDate;
    public String mVehicleRents;
    public String mAddedServiceFee;
    public String mOtherFee;
}