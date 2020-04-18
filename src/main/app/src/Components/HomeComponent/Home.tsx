import React, { useState, useEffect, useContext, useCallback, ReactElement } from 'react';

import { ILandscape } from '../../interfaces';
import GenericModal from '../ModalComponent/GenericModal';
import LandscapeLog from '../LandscapeComponent/Log/LandscapeLog';
import Command from '../CommandComponent/Command';
import { Link } from 'react-router-dom';
// import { createStyles, Theme, makeStyles } from '@material-ui/core/styles';
import CommandContext from '../../Context/Command.context';
import LandscapeContext from '../../Context/Landscape.context';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import { Button } from '@material-ui/core';
import './Home.scss';
import Events from './Events';

/*const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    marginBottom: {
      marginBottom: theme.spacing(1),
    },
    extendedIcon: {
      marginRight: theme.spacing(1),
    },
  })
);*/

/**
 * Displays all available landscapes and provides all needed navigation
 */

const Home: React.FC = () => {
  const [modalContent, setModalContent] = useState<string | ReactElement | ReactElement[] | null>(
    null
  );
  const [landscapes, setLandscapes] = useState<ILandscape[]>();
  const [loadLandscapes, setLoadLandscapes] = useState<boolean>(true);

  const commandContext = useContext(CommandContext);
  const landscapeContext = useContext(LandscapeContext);
  //Could be moved into useEffect but can be used for a reload button later on
  const getLandscapes = useCallback(async () => {
    if (loadLandscapes) {
      await fetch(process.env.REACT_APP_BACKEND_URL + '/api/')
        .then((response) => {
          return response.json();
        })
        .then((json) => {
          setLandscapes(json);
          setLoadLandscapes(false);
          landscapeContext.landscapes = json;
          commandContext.message = 'Loaded landscapes.';
        });
    }
  }, [commandContext.message, landscapeContext.landscapes, loadLandscapes]);

  //ComponentDidMount
  useEffect(() => {
    getLandscapes();
  }, [getLandscapes]);

  const enterLog = (landscape: ILandscape) => {
    setModalContent(<LandscapeLog landscape={landscape} />);
    commandContext.message = 'Showing log: ' + landscape.identifier;
  };

  const showEvents = () => {
    setModalContent(<Events />);
    commandContext.message = 'Showing events.';
  };

  const enterLandscape = (landscape: ILandscape) => {
    commandContext.message = 'Entering landscape: ' + landscape.identifier;
  };
  // Render
  let content: string | ReactElement[] = 'Loading landscapes...';
  if (landscapes) {
    content = landscapes.map((landscape) => {
      return (
        <Grid key={landscape.identifier} className={'landscapeContainer'} container spacing={3}>
          <Grid item xs={12} sm={12}>
            <Grid container className={'bar'} spacing={2}>
              <Grid item xs={1} sm={1} className={'first item'}></Grid>
              <Grid item xs={4} sm={3} className={'title'}>
                {landscape.name}
              </Grid>
              <Grid item xs={4} sm={5} className={'item'}></Grid>
              <Grid item xs={1} sm={1} className={'no-item'}>
                <Button
                  onClick={() => enterLandscape(landscape)}
                  fullWidth
                  component={Link}
                  className={'button'}
                  to={`/landscape/${landscape.identifier}`}
                >
                  enter
                </Button>
              </Grid>
              <Grid item xs={1} sm={1} className={'no-item'}>
                <Button onClick={() => enterLog(landscape)} fullWidth className={'button'}>
                  log
                </Button>
              </Grid>
              <Grid item xs={1} sm={1} className={'item'}></Grid>
            </Grid>
          </Grid>

          <Grid item xs={1} className={'previewItem'}>
            <Button
              component={Link}
              to={`/landscape/${landscape.identifier}`}
              onClick={() => enterLandscape(landscape)}
            >
              <img
                className={'preview'}
                alt={'preview'}
                src={
                  process.env.REACT_APP_BACKEND_URL +
                  '/render/' +
                  landscape.identifier +
                  '/graph.png'
                }
                style={{ maxWidth: 100, float: 'left' }}
              />
            </Button>
          </Grid>
          <Grid item xs={3}>
            <Typography variant='overline' display='block' gutterBottom>
              Info
            </Typography>
            {landscape.description}
            <br />
            <br />
            Identifier: {landscape.identifier}
            <br />
            Contact: {landscape.contact || '-'}
            <br />
            Teams: {landscape.stats.teams.join(', ')}
            <br />
          </Grid>

          <Grid item xs={2}>
            <Typography variant='overline' display='block' gutterBottom>
              State
            </Typography>
            {landscape.stats.overallState || '-'}
          </Grid>

          <Grid item xs={2}>
            <Typography variant='overline' display='block' gutterBottom>
              Items
            </Typography>
            <Typography variant='h2' display='block' gutterBottom>
              {landscape.stats.items}
            </Typography>
            in {landscape.stats.groups} groups
          </Grid>

          <Grid item xs={2}>
            <Typography variant='overline' display='block' gutterBottom>
              Last update
            </Typography>
            <Typography variant='h3' display='block'>
              {landscape.stats.lastUpdate?.split(' ')[0] || '-'}
            </Typography>

            <div>{landscape.stats.lastUpdate?.split(' ')[1] || '-'}</div>
          </Grid>

          <Grid item xs={2}>
            <Typography variant='overline' display='block' gutterBottom>
              More
            </Typography>
            <a
              target={'_blank'}
              rel='noopener noreferrer'
              href={
                process.env.REACT_APP_BACKEND_URL + '/render/' + landscape.identifier + '/map.svg'
              }
            >
              Printable Graph
            </a>
            <br />
            <a
              target={'_blank'}
              rel='noopener noreferrer'
              href={
                process.env.REACT_APP_BACKEND_URL + '/docs/' + landscape.identifier + '/report.html'
              }
            >
              Printable Report
            </a>
          </Grid>
        </Grid>
      );
    });
  }

  return (
    <div className='homeContainer'>
      <GenericModal modalContent={modalContent} />
      <Grid container spacing={2} className={'header'}>
        <Grid item xs={8} sm={9} className={'first'}></Grid>
        <Grid item xs={3} sm={2} className={'title'}>
          Nivio
        </Grid>
        <Grid item xs={1} sm={1} className={'last'}></Grid>
      </Grid>

      <Grid container spacing={2} className={'content'}>
        <Grid item xs={2} sm={1} className={'elbow1'}></Grid>
        <Grid item xs={1} sm={1} className={'elbow2'}>
          <div className={'elbow-outer'}>
            <div className={'elbow-inner'}></div>
          </div>
        </Grid>
        <Grid item></Grid>
      </Grid>

      <Grid container spacing={2} className={'content'}>
        <Grid item xs={2} sm={1} className={'sidebar'}>
          <div className={'item'}></div>

          <Button component={Link} to={``} fullWidth className={'item button'}>
            Home
          </Button>

          <Button component={Link} to={``} fullWidth className={'item button'}>
            Guide
          </Button>

          <Button
            data-testid='ManualButton'
            component={Link}
            to={`/man/install`}
            fullWidth
            className={'item button'}
          >
            Manual
          </Button>

          <Button
            data-testid='EventsButton'
            onClick={() => showEvents()}
            fullWidth
            className={'item button'}
          >
            Events
          </Button>

          <div className={'item'}></div>
        </Grid>
        <Grid item xs={1} sm={1} className={'spacer'}></Grid>
        <Grid item xs={9} sm={10}>
          {content}
        </Grid>
      </Grid>

      <Command />
    </div>
  );
};

export default Home;