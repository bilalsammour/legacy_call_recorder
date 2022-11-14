package com.threebanders.recordrs.common

/**
 * Ca să nu lansez dialoguri din presenter metodele de aici întorc un obiect care conține informații pe baza
 * cărora se poate construi un dialog în fragment.
 */
class DialogInfo(var title: Int, var message: Int, var icon: Int)