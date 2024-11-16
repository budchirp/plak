use adw::{
    prelude::*, ApplicationWindow, Dialog, EntryRow, HeaderBar, Toast, ToastOverlay, ToolbarView,
    WindowTitle,
};
use async_channel::Sender;
use gtk::{gio, Button, DropDown, Orientation, StringList};

use crate::{
    config::instance_config::{InstanceConfigManager, InstanceConfigStruct},
    minecraft::VersionManifest,
    network::Network,
};

pub struct AddInstanceDialog {}

impl AddInstanceDialog {
    pub async fn new(
        window: ApplicationWindow,
        toast_overlay: ToastOverlay,
        sender: Sender<String>,
    ) {
        let dialog_content = gtk::Box::builder()
            .orientation(Orientation::Vertical)
            .spacing(12)
            .margin_start(12)
            .margin_end(12)
            .margin_top(12)
            .margin_bottom(12)
            .build();

        let name_entry = EntryRow::builder()
            .title("Name")
            .css_classes(["card"])
            .build();
        dialog_content.append(&name_entry);

        let slug_entry = EntryRow::builder()
            .title("Slug")
            .css_classes(["card"])
            .build();
        dialog_content.append(&slug_entry);

        let version_dropdown = DropDown::builder()
            .model(&StringList::new(&["Select version"]))
            .build();
        dialog_content.append(&version_dropdown);

        let submit_button = Button::builder()
            .label("Submit")
            .css_classes(["suggested-action"])
            .build();
        dialog_content.append(&submit_button);

        let dialog_toolbar = ToolbarView::builder().content(&dialog_content).build();

        let dialog_header_bar_title = WindowTitle::new("Add instance", "");
        let dialog_header_bar = HeaderBar::builder()
            .title_widget(&dialog_header_bar_title)
            .build();
        dialog_toolbar.add_top_bar(&dialog_header_bar);

        let dialog = Dialog::builder().child(&dialog_toolbar).build();

        // TODO: Fetch the data in a new thread
        let network = Network::new();
        let manifest = network
            .fetch::<VersionManifest>(
                "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json",
            )
            .unwrap();
        let versions: Vec<&str> = manifest
            .versions
            .iter()
            .map(|version| version.id.as_str())
            .collect();

        let list_store = StringList::new(&versions);
        version_dropdown.set_model(Some(&list_store));

        let dialog_clone = dialog.clone();
        submit_button.connect_clicked(move |_| {
            let dialog = &dialog_clone;

            let name = name_entry.text();
            let slug = slug_entry.text();

            let version_index = version_dropdown.selected();
            if let Some(version) = list_store.string(version_index) {
                let instance_config = InstanceConfigManager::new(slug.as_str());
                let result = instance_config.set(&InstanceConfigStruct {
                    downloaded: false,

                    image_path: "".to_string(),

                    slug: slug.to_string(),
                    name: name.to_string(),

                    version: version.to_string(),

                    main_class: "".to_string(),

                    asset_index: "".to_string(),
                });

                toast_overlay.add_toast(Toast::new(&if let Err(error) = result {
                    error.to_string()
                } else {
                    "Successfully added".to_string()
                }));

                dialog.close();

                let sender_clone = sender.clone();
                gio::spawn_blocking(move || {
                    sender_clone
                        .send_blocking("refresh".to_string())
                        .expect("Failed to send message");
                });
            }
        });

        dialog.present(Some(&window));
    }
}
