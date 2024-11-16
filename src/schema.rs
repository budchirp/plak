// @generated automatically by Diesel CLI.

diesel::table! {
    accounts (id) {
        id -> Nullable<Integer>,
        username -> Text,
        token -> Text,
        uuid -> Text,
    }
}
