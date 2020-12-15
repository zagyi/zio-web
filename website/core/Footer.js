/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

class Footer extends React.Component {
  docUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    const docsUrl = this.props.config.docsUrl;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    return `${baseUrl}${docsPart}${langPart}${doc}`;
  }

  pageUrl(doc, language) {
    const baseUrl = this.props.config.baseUrl;
    return baseUrl + (language ? `${language}/` : '') + doc;
  }

  render() {
    return (
      <footer className="nav-footer" id="footer">
        <section className="sitemap">
          <a href={this.props.config.baseUrl} className="nav-home">
            {this.props.config.footerIcon && (
              <img
                src={this.props.config.baseUrl + this.props.config.footerIcon}
                alt={this.props.config.title}
              />
            )}
          </a>
          <div>
            <h5>GitHub</h5>
            <a href={this.props.config.shields.github.link}><img src={this.props.config.shields.github.image} alt="github"/></a>
          </div>
          <div>
              <h5>Chat with us on Discord</h5>
              <a href={this.props.config.shields.discord.link}><img src={this.props.config.shields.discord.image} alt="discord"/></a>
          </div>
          <div>
              <h5>Follow us on Twitter</h5>
              <a href={this.props.config.shields.twitter.link}><img src={this.props.config.shields.twitter.image} alt="twitter"/></a>
          </div>
          <div>
            <h5>Additional resources</h5>
            <a href="https://zio.dev">ZIO Homepage</a>
          </div>
        </section>
        <section className="copyright">{this.props.config.copyright}</section>
      </footer>
    );
  }
}

module.exports = Footer;
