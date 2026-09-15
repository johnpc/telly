package com.johncorser.telly.features.mylist

/** My-list seams a menu host needs: the store + the management screens. */
class MyListHooks(
    val onOpenManageFavorites: () -> Unit = {},
    val onOpenReorderChannels: (String) -> Unit = {},
    /** Saved My-list programmes behind the sheet's "Add to My list". */
    val store: MyListStore = InMemoryMyListStore(),
)
