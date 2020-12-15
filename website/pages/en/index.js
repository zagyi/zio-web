/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const MarkdownBlock = CompLibrary.MarkdownBlock; /* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class HomeSplash extends React.Component {
  render() {
    const {siteConfig, language = ''} = this.props;
    const {baseUrl, docsUrl} = siteConfig;
    const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
    const langPart = `${language ? `${language}/` : ''}`;
    const docUrl = (doc) => `${baseUrl}${docsPart}${langPart}${doc}`;

    const SplashContainer = (props) => (
      <div className="homeContainer">
        <div className="homeSplashFade">
          <div className="wrapper homeWrapper">{props.children}</div>
        </div>
      </div>
    );

    const LogoTitle = (props) => (
      <div className="logoTitle">
        <img src={props.logo} alt="Project Logo" />
        {props.title}
      </div>
    );

    const ProjectTitle = (props) => (
      <h2 className="projectTitle">
        <LogoTitle logo={`${baseUrl}${siteConfig.logo}`} title={props.title} />
        <small>{props.tagline}</small>
      </h2>
    );

    const PromoSection = (props) => (
      <div className="section promoSection">
        <div className="promoRow">
          <div className="pluginRowBlock">{props.children}</div>
        </div>
      </div>
    );

    const Button = (props) => (
      <div className="pluginWrapper buttonWrapper">
        <a className="button" href={props.href} target={props.target}>
          {props.children}
        </a>
      </div>
    );

    return (
      <SplashContainer>
        <div className="inner">
          <ProjectTitle tagline={siteConfig.tagline} title={siteConfig.title} />
          <PromoSection>
            <Button href={docUrl('getting_started.html')}>Get Started</Button>
          </PromoSection>
        </div>
      </SplashContainer>
    );
  }
}

class Index extends React.Component {
  render() {
    const {config: siteConfig, language = ''} = this.props;
    const {baseUrl} = siteConfig;

    const Block = (props) => (
      <Container
        padding={['bottom', 'top']}
        id={props.id}
        background={props.background}>
        <GridBlock
          align="center"
          contents={props.children}
          layout={props.layout}
        />
      </Container>
    );

    const Features = () => (
      <Block layout="threeColumn">
        {[
          {
            content: 'Define reliable and scalable endpoints concisely, type-safely, and composably.',
            title: 'Endpoints',
          },
          {
            content: 'Deploy endpoints to any supported protocol, including HTTP and gRPC.',
            title: 'Protocol Agnostic',
          },
          {
            content: 'Handle requests and responses that are too big to fit in memory at once.',
            title: 'Stream Friendly',
          },
          {
            content:
              'Generate documentation that is automatically in-sync with the endpoints. ' +
              'Interact with an endpoint type-safely from Scala without writing any code.',
            title: 'Introspection Friendly',
          },
          {
            content:
              'Middleware is type-safe and compositional and can include built-in middleware or third-party ' +
              'middleware, or combinations.',
            title: 'Middleware Friendly',
          },
          {
            content: 'Fastest functional Scala library.',
            title: 'High Performance',
          },
        ]}
      </Block>
    );

    return (
      <div>
        <HomeSplash siteConfig={siteConfig} language={language} />
        <div className="mainContainer">
          <Features />
        </div>
      </div>
    );
  }
}

module.exports = Index;
