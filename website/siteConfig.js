/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

// See https://docusaurus.io/docs/site-config for all the possible
// site configuration options.

// List of projects/orgs using your project for the users page.
const users = [
  /*
  {
    caption: 'User1',
    // You will need to prepend the image path with your baseUrl
    // if it is not '/', like: '/test-site/img/image.jpg'.
    image: '/img/undraw_open_source.svg',
    infoLink: 'https://www.facebook.com',
    pinned: true,
  },
  */
];

const siteConfig = {
  title: 'ZIO Web',
  tagline: 'ZIO-powered cloud services via HTTP and other protocols',
  url: 'https://zio.github.io',
  baseUrl: '/zio-web/',

  // Used for publishing and more
  projectName: 'zio-web',
  organizationName: 'zio',

  // For no header links in the top nav bar -> headerLinks: [],
  headerLinks: [
    { doc: 'getting_started', label: 'Getting Started' },
    { doc: 'datatypes/index', label: 'Data Types' },
  ],

  // If you have users set above, you add it here:
  users,

  /* path to images for header/footer */
  headerIcon: 'img/navbar_brand.png',
  footerIcon: 'img/navbar_brand.png',
  favicon: 'img/favicon.png',
  logo: 'img/navbar_brand2x.png',

  /* Colors for website */
  colors: {
    primaryColor: '#000000',
    secondaryColor: '#121020',
  },

  /* Custom fonts for website */
  /*
  fonts: {
    myFont: [
      "Times New Roman",
      "Serif"
    ],
    myOtherFont: [
      "-apple-system",
      "system-ui"
    ]
  },
  */

  // This copyright info is used in /core/Footer.js and blog RSS/Atom feeds.
  copyright: `Copyright © ${new Date().getFullYear()} ZIO Maintainers`,

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks.
    theme: 'default',
  },

  // Add custom scripts here that would be placed in <script> tags.
  scripts: ['https://buttons.github.io/buttons.js'],

  // On page navigation for the current documentation page.
  onPageNav: 'separate',
  // No .html extensions for paths.
  cleanUrl: true,

  // Open Graph and Twitter card images.
  // ogImage: 'img/undraw_online.svg',
  // twitterImage: 'img/undraw_tweetstorm.svg',

  // For sites with a sizable amount of content, set collapsible to true.
  // Expand/collapse the links and subcategories under categories.
  // docsSideNavCollapsible: true,

  // Show documentation's last contributor's name.
  enableUpdateBy: true,

  // Show documentation's last update time.
  enableUpdateTime: true,

  // You may provide arbitrary config keys to be used as needed by your
  // template. For example, if you need your repo's URL...
  // repoUrl: 'https://github.com/facebook/test-site',

  scrollToTop: true,
  scrollToTopOptions: {
    cornerOffset: 100,
  },

  customDocsPath: 'zio-web-docs/target/mdoc',

  shields: {
    github: {
      link: 'https://github.com/zio/zio-web',
      image: 'https://img.shields.io/github/stars/zio/zio-web?style=social'
    },
    discord: {
      link: 'https://discord.gg/yjYVc3hH',
      image: 'https://img.shields.io/discord/629491597070827530?logo=discord&style=social'
    },
    twitter: {
      link: 'https://twitter.com/zioscala',
      image: 'https://img.shields.io/twitter/follow/zioscala?label=Follow&style=social'
    },
  }
};

module.exports = siteConfig;
