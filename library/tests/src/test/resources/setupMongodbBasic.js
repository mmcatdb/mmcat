db.dropDatabase();

db.order.insertMany([
    {
        number: "o_100",
    },
    {
        number: "o_200",
    },
]);

db.address.insertMany([
    {
        number: "o_100",
        address: {
            street: "Ke Karlovu 2027/3",
            city: "Praha 2",
            zip: "121 16",
        },
    },
    {
        number: "o_200",
        address: {
            street: "Malostranské nám. 2/25",
            city: "Praha 1",
            zip: "118 00",
        },
    },
]);

db.tag.insertMany([
    {
        number: "o_100",
        tags: [
            "123",
            "456",
            "789",
        ],
    },
    {
        number: "o_200",
        tags: [
            "123",
            "String456",
            "String789",
        ],
    },
]);

db.orderItem.insertMany([
    {
        number: "o_100",
        items: [
            { id: "123", label: "Clean Code", price: "125", quantity: "1" },
        ],
    },
    {
        number: "o_100",
        items: [
            { id: "765", label: "The Lord of the Rings", price: "199", quantity: "2" },
        ],
    },
    {
        number: "o_200",
        items: [
            { id: "457", label: "The Art of War", price: "299", quantity: "7" },
            { id: "734", label: "Animal Farm", price: "350", quantity: "3" },
        ],
    },
]);

db.orderItemEmpty.insertMany([
    {
        number: "o_100",
        items: null,
    },
    {
        number: "o_200",
        items: [],
    },
]);

db.contact.insertMany([
    {
        number: "o_100",
        contact: {
            "phone": "123456789",
            "email": "alice@mmcatdb.com",
        },
    },
    {
        number: "o_200",
        contact: {
            "email": "bob@mmcatdb.com",
            "github": "https://github.com/mmcatdb",
        },
    },
]);

db.customer.insertMany([
    {
        number: "o_100",
        customer: {
            name: "Alice",
            number: "c_100",
        },
    },
    {
        number: "o_200",
        customer: {
            name: "Bob",
            number: "c_200",
        },
    },
]);

db.note.insertMany([
    {
        number: "o_100",
        note: {
            'en-US': { subject: "subject 1", content: "content en" },
            'cs-CZ': { subject: "subject 1", content: "content cz" },
        },
    },
    {
        number: "o_200",
        note: {
            'cs-CZ': { subject: "subject cz", content: "content 1" },
            'en-GB': { subject: "subject gb", content: "content 2" },
        },
    },
]);

db.inference.insertMany([
    {
        meta: {
            language: "en",
            generated: "2023-07-10 12:18:23",
        },
        nodes: {
            element_2: {
                type: "element",
                label: "Oral heritage of Gelede",
                meta: {
                    icon: {
                        small: "https://ich.unesco.org/img/photo/thumb/00365-MED.jpg",
                        large: "https://ich.unesco.org/img/photo/thumb/00365-HUG.jpg",
                    },
                    description: "The Gelede is performed by the Yoruba-Nago community that is spread over Benin, Nigeria and Togo. For more than a century, this ceremony has been performed to pay tribute to the primordial mother Iyà Nlà and to the role women play in the process of social organization and development of Yoruba society. <br><br>The Gelede takes place every year after the harvests, at important events and during drought or epidemics and is characterized by carved masks, dances and chants, sung in the Yoruba language and retracing the history and myths of the Yoruba-Nago people. The ceremony usually takes place at night on a public square and the dancers prepare in a nearby house. The singers and the drummers are the first to appear. They are accompanied by an orchestra and followed by the masked dancers wearing splendid costumes. There is a great deal of preparatory craftwork involved, especially mask carving and costume making. The performances convey an oral heritage that blends epic and lyric verses, which employ a good deal of irony and mockery, supported by satirical masks. Figures of animals are often used, such as the serpent, a symbol of power, or the bird, the messenger of the “mothers”. The community is divided into groups of men and women led by a male and a female head. It is the only known masked society, which is also governed by women. Although the Gelede has nowadays adapted to a more patriarchal society, the oral heritage and dances can be considered as a testimony of the former matriarchal order. <br><br>Technical development is resulting in a gradual loss of traditional know-how, and tourism is jeopardizing the Gelede by turning it into a folklore product. Nevertheless, the Gelede community shows great awareness of the value of their intangible heritage, which is reflected in the efforts put into the preparation work and in the growing number of participants.",
                    list: "RL",
                    year: 2008,
                    multinational: true,
                    link: "https://ich.unesco.org/en/RL/oral-heritage-of-gelede-00002",
                    images: [
                        {
                            url: "https://ich.unesco.org/img/photo/thumb/00015-BIG.jpg",
                            copyright: " UNESCO/Yoshihiro Higuchi",
                            title: "The Gelede ceremony, held in honour of the primordial mother Iyà Nlà, is directed by women, reflecting their important role in Yoruba life. After weeks of preparations, singers and drummers open the night-time ceremony, followed by an orchestra and masked dancers wearing\r\nsplendid costumes.",
                        },
                        {
                            url: "https://ich.unesco.org/img/photo/thumb/00371-BIG.jpg",
                            copyright: " UNESCO/ Yoshihiro Higuchi",
                            title: "",
                        }
                    ],
                    video: [
                        {
                            url: "https://www.youtube.com/watch?v=ycSMt0bjE1c",
                            copyright: "",
                            title: "Sauvegarde et protection",
                        },
                        {
                            url: "https://www.youtube.com/watch?v=PsqWRtFWdMo",
                            copyright: "",
                            title: "Sauvegarde et protection",
                        },
                        {
                            url: "https://www.youtube.com/watch?v=2a5Fq0pkBIM",
                            copyright: "",
                            title: "Sauvegarde et protection",
                        },
                    ],
                    sustainability: "",
                },
            },
        },
    },
]);
